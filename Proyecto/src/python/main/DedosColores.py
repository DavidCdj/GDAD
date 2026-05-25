import cv2
import sys
import random
import time
import numpy as np
from collections import Counter
import math
from pathlib import Path
from PIL import Image, ImageSequence

# Evitamos que Python guarde en búfer las impresiones para que Java las lea al instante
sys.stdout.reconfigure(line_buffering=True)

# Importación blindada de MediaPipe a prueba de errores de Windows
try:
    from mediapipe.solutions import hands as mp_hands
    from mediapipe.solutions import drawing_utils as mp_drawing
except ImportError:
    import mediapipe.python.solutions.hands as mp_hands
    import mediapipe.python.solutions.drawing_utils as mp_drawing

hands = mp_hands.Hands(
    static_image_mode=False,
    max_num_hands=2,  
    min_detection_confidence=0.7,  
    min_tracking_confidence=0.7    
)

# Colores en formato BGR para OpenCV
COLORES = {
    "Azul": (255, 100, 0), "Rojo": (0, 0, 255), "Verde": (100, 255, 100),
    "Amarillo": (0, 255, 255), "Morado": (200, 0, 150)
}
LISTA_COLORES = list(COLORES.keys())

DEDO_A_COLOR = {"Pulgar": "Azul", "Indice": "Rojo", "Medio": "Verde", "Anular": "Amarillo", "Menique": "Morado"}
COLOR_A_DEDO = {v: k for k, v in DEDO_A_COLOR.items()}
DEDOS_IDS = {"Pulgar": 4, "Indice": 8, "Medio": 12, "Anular": 16, "Menique": 20}
TODOS_LOS_DEDOS = ["Pulgar", "Indice", "Medio", "Anular", "Menique"]
DEBUG_CAPTURA = False

RESULTADO_PATH = Path("recursos") / "resultado_dedos_colores.txt"

BG_MAIN = (179, 226, 255)

TOTAL_RONDAS = 4
ELEMENTOS_POR_RONDA = 5

def crear_secuencia():
    return [random.choice(LISTA_COLORES) for _ in range(ELEMENTOS_POR_RONDA)]


def mostrar_pantalla_tutorial(ruta_gif, segundos_duracion=15):
    try:
        img_gif = Image.open(ruta_gif)
        frames_gif = []
        for frame in ImageSequence.Iterator(img_gif):
            frame_bgr = cv2.cvtColor(np.array(frame.convert("RGB")), cv2.COLOR_RGB2BGR)
            frame_bgr = cv2.resize(frame_bgr, (500, 400))
            frames_gif.append(frame_bgr)
    except Exception:
        return

    cv2.namedWindow("Actividad: Gimnasia Bimanual", cv2.WINDOW_NORMAL)
    cv2.resizeWindow("Actividad: Gimnasia Bimanual", 1280, 720)

    num_frames = len(frames_gif)
    contador_frame = 0
    tiempo_inicio_tutorial = time.time()

    while True:
        tiempo_transcurrido = time.time() - tiempo_inicio_tutorial
        if tiempo_transcurrido >= segundos_duracion:
            break

        pantalla_tutorial = np.full((720, 1280, 3), BG_MAIN, dtype=np.uint8)

        frame_actual = frames_gif[contador_frame]
        pantalla_tutorial[180:580, 390:890] = frame_actual
        contador_frame = (contador_frame + 1) % num_frames

        cv2.imshow("Actividad: Gimnasia Bimanual", pantalla_tutorial)
        key = cv2.waitKey(60) & 0xFF
        if key == 27:
            cv2.destroyAllWindows()
            sys.exit(0)


mostrar_pantalla_tutorial("recursos/VideoDedosColores.gif", segundos_duracion=15)


secuencia_izquierda = crear_secuencia()
secuencia_derecha = crear_secuencia()
indice_actual = 0
ronda_actual = 1

aciertos = 0
errores = 0
ambos_correctos_sostenido = False
tiempo_inicio_acierto = 0
TIEMPO_REQUERIDO = 1.0  
ultimo_tiempo_error = 0

# Historial para suavizado del dedo dominante por mano
HISTORIAL_SIZE = 5
dedos_detectados_historial = {"Left": [], "Right": []}

# Historial de presencia por dedo (útil cuando la moda del dominante falla)
dedos_presencia_historial = {
    "Left": {d: [] for d in TODOS_LOS_DEDOS},
    "Right": {d: [] for d in TODOS_LOS_DEDOS}
}


prev_region_mapping = {"left_region": None, "right_region": None}

# Umbrales y márgenes para detección (ajusta y prueba)
MARGEN_HORIZONTAL_PULGAR = 0.04
# Pruebas sugeridas:
# MARGEN_HORIZONTAL_PULGAR = 0.03
# MARGEN_HORIZONTAL_PULGAR = 0.06

MARGEN_VERTICAL_DEDO = 0.05
# Pruebas sugeridas:
# MARGEN_VERTICAL_DEDO = 0.03
# MARGEN_VERTICAL_DEDO = 0.07

# Relación mínima entre la distancia punta-mcp y el tamaño de la mano para considerar dedo extendido
TIP_MCP_RATIO = 0.35
# Pruebas sugeridas:
# TIP_MCP_RATIO = 0.30
# TIP_MCP_RATIO = 0.40

# Parámetros para la selección de dedo dominante (scores)
DOMINANT_SCORE_THRESHOLD = 0.03
DOMINANT_SCORE_GAP = 0.02
# Pruebas sugeridas:
# DOMINANT_SCORE_THRESHOLD = 0.02
# DOMINANT_SCORE_GAP = 0.01


# --- FUNCIÓN ROBUSTA DE CÁMARA ---
def open_working_camera(max_index=5, preferred_index=None):
    import os
    # Check env or CLI for preferred index
    pref = preferred_index
    if pref is None:
        env = os.environ.get("CAMERA_INDEX")
        if env:
            try:
                pref = int(env)
            except Exception:
                pref = None
    # CLI arg --camera-index N
    if pref is None:
        for idx, arg in enumerate(sys.argv):
            if arg.startswith("--camera-index"):
                parts = arg.split("=")
                if len(parts) == 2:
                    try:
                        pref = int(parts[1])
                    except Exception:
                        pref = None
                else:
                    # next arg
                    try:
                        pref = int(sys.argv[idx+1])
                    except Exception:
                        pref = None
                break

    backends = [("ANY", cv2.CAP_ANY), ("MSMF", cv2.CAP_MSMF), ("DSHOW", cv2.CAP_DSHOW)]

    def try_index(i, backend, name):
        try:
            cap = cv2.VideoCapture(i, backend)
            if not cap or not cap.isOpened():
                try: cap.release()
                except Exception: pass
                return None
            ret, frame = cap.read()
            if ret and frame is not None:
                h, w = frame.shape[:2]
                return cap
            try: cap.release()
            except Exception: pass
        except Exception:
            pass
        return None

    # Try preferred index first if provided
    if pref is not None:
        for name, backend in backends:
            cap = try_index(pref, backend, name)
            if cap is not None:
                return cap

    # Try all backends and indexes
    for name, backend in backends:
        for i in range(max_index):
            # skip pref if already tried
            if pref is not None and i == pref:
                continue
            cap = try_index(i, backend, name)
            if cap is not None:
                return cap

    return None


def detectar_dedos_levantados(puntos, mano_lado):
    margen = MARGEN_HORIZONTAL_PULGAR
    margen_vertical = MARGEN_VERTICAL_DEDO

    # Calcular tamaño de la mano (norma entre muñeca y middle_mcp)
    try:
        hand_size = math.sqrt((puntos[9].x - puntos[0].x)**2 + (puntos[9].y - puntos[0].y)**2)
    except Exception:
        hand_size = 0.1

    def tip_mcp_ratio(tip_id, mcp_id):
        try:
            d = math.sqrt((puntos[tip_id].x - puntos[mcp_id].x)**2 + (puntos[tip_id].y - puntos[mcp_id].y)**2)
            if hand_size <= 0: return 0
            return d / hand_size
        except Exception:
            return 0

    dedos_arriba = {}
    if mano_lado == "Left":
        pulgar_ok = puntos[4].x > (puntos[3].x + margen)
    else:
        pulgar_ok = puntos[4].x < (puntos[3].x - margen)
    # Reforzar condición del pulgar con distancia normalizada
    pulgar_ok = pulgar_ok and (tip_mcp_ratio(4, 2) >= TIP_MCP_RATIO)
    dedos_arriba["Pulgar"] = pulgar_ok

    dedos_arriba["Indice"] = (puntos[8].y < (puntos[6].y - margen_vertical)) and (tip_mcp_ratio(8, 5) >= TIP_MCP_RATIO)
    dedos_arriba["Medio"] = (puntos[12].y < (puntos[10].y - margen_vertical)) and (tip_mcp_ratio(12, 9) >= TIP_MCP_RATIO)
    dedos_arriba["Anular"] = (puntos[16].y < (puntos[14].y - margen_vertical)) and (tip_mcp_ratio(16, 13) >= TIP_MCP_RATIO)
    dedos_arriba["Menique"] = (puntos[20].y < (puntos[18].y - margen_vertical)) and (tip_mcp_ratio(20, 17) >= TIP_MCP_RATIO)

    return {dedo for dedo, levantado in dedos_arriba.items() if levantado}, dedos_arriba


def obtener_dedo_dominante(puntos, mano_lado):
    scores = {}

    if mano_lado == "Left":
        scores["Pulgar"] = puntos[4].x - puntos[3].x
    else:
        scores["Pulgar"] = puntos[3].x - puntos[4].x

    scores["Indice"] = puntos[6].y - puntos[8].y
    scores["Medio"] = puntos[10].y - puntos[12].y
    scores["Anular"] = puntos[14].y - puntos[16].y
    scores["Menique"] = puntos[18].y - puntos[20].y

    candidatos = [(dedo, score) for dedo, score in scores.items() if score > DOMINANT_SCORE_THRESHOLD]
    if not candidatos:
        return None, scores

    candidatos.sort(key=lambda item: item[1], reverse=True)
    dedo_dominante, score_dominante = candidatos[0]
    segundo_score = candidatos[1][1] if len(candidatos) > 1 else 0

    if score_dominante < DOMINANT_SCORE_THRESHOLD or (score_dominante - segundo_score) < DOMINANT_SCORE_GAP:
        return None, scores

    return dedo_dominante, scores


def texto_dedos(dedos):
    return "-" if not dedos else ",".join(sorted(dedos))

cap = open_working_camera(6)

if cap is None:
    RESULTADO_PATH.parent.mkdir(parents=True, exist_ok=True)
    RESULTADO_PATH.write_text("RESULTADO:0,0", encoding="utf-8")
    sys.exit(1)

# Creamos la ventana nativa de OpenCV
cv2.namedWindow("Actividad: Gimnasia Bimanual", cv2.WINDOW_NORMAL)
cv2.resizeWindow("Actividad: Gimnasia Bimanual", 1280, 720)

while cap.isOpened():
    success, frame = cap.read()
    if not success: break

    # Lienzo gris claro para el Dashboard de fondo
    dashboard = np.full((720, 1280, 3), BG_MAIN, dtype=np.uint8) 

    if ronda_actual > TOTAL_RONDAS:
        break
        
    color_obj_izq = secuencia_izquierda[indice_actual]
    color_obj_der = secuencia_derecha[indice_actual]
    
    # --- PROCESAMIENTO MEDIA PIPE ---
    frame = cv2.flip(frame, 1)  
    rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    results = hands.process(rgb_frame)

    alto_cam, ancho_cam, _ = frame.shape
    dedos_detectados = {"Left": None, "Right": None}
    # conjuntos de dedos detectados por frame (para mostrar y validar inmediatamente)
    dedos_actuales_sets = {"Left": set(), "Right": set()}

    detected_sides_current = {"Left": False, "Right": False}
    if results.multi_hand_landmarks and results.multi_handedness:
        for idx, hand_handedness in enumerate(results.multi_handedness):
            if idx >= len(results.multi_hand_landmarks): continue
            try:
                detected_label = hand_handedness.classification[0].label
                hand_landmarks = results.multi_hand_landmarks[idx]
                mp_drawing.draw_landmarks(frame, hand_landmarks, mp_hands.HAND_CONNECTIONS)
                puntos = hand_landmarks.landmark
                if len(puntos) < 21: continue

                # Determinar lado por posición horizontal de la muñeca (landmark 0)
                try:
                    wrist_x = puntos[0].x
                except Exception:
                    wrist_x = 0.5
                mano_lado = "Left" if wrist_x < 0.5 else "Right"
                # Detectar si la asociación región(horizontal) -> lado ha cambiado; si es así,
                # limpiamos historiales para evitar contaminación entre manos.
                region_key = "left_region" if wrist_x < 0.5 else "right_region"
                prev = prev_region_mapping.get(region_key)
                if prev is None:
                    prev_region_mapping[region_key] = mano_lado
                elif prev != mano_lado:
                    for s in ("Left", "Right"):
                        try:
                            dedos_detectados_historial[s].clear()
                        except Exception:
                            pass
                        try:
                            for d in TODOS_LOS_DEDOS:
                                dedos_presencia_historial[s][d].clear()
                        except Exception:
                            pass
                    prev_region_mapping[region_key] = mano_lado

                dedos_actuales, dedos_arriba = detectar_dedos_levantados(puntos, mano_lado)
                dedo_dominante, scores = obtener_dedo_dominante(puntos, mano_lado)

                for d_name, d_id in DEDOS_IDS.items():
                    px, py = int(puntos[d_id].x * ancho_cam), int(puntos[d_id].y * alto_cam)
                    color_nombre = DEDO_A_COLOR.get(d_name, "Azul")
                    radio = 12 if dedos_arriba.get(d_name) else 6
                    cv2.circle(frame, (px, py), radio, COLORES[color_nombre], -1)

                # Guardar el dedo dominante (o None) en el historial para suavizar errores
                dedos_detectados[mano_lado] = dedo_dominante
                # Guardar también el conjunto de dedos detectados en este frame
                dedos_actuales_sets[mano_lado] = dedos_actuales

                detected_sides_current[mano_lado] = True
                hist = dedos_detectados_historial.get(mano_lado)
                hist.append(dedo_dominante)
                if len(hist) > HISTORIAL_SIZE:
                    hist.pop(0)

                # Guardar presencia de cada dedo en su historial para mayor robustez
                try:
                    for d in TODOS_LOS_DEDOS:
                        pres = bool(dedos_arriba.get(d))
                        ph = dedos_presencia_historial[mano_lado][d]
                        ph.append(pres)
                        if len(ph) > HISTORIAL_SIZE:
                            ph.pop(0)
                except Exception:
                    pass

                # (Etiqueta de muñeca ocultada: no se dibuja para mantener la vista limpia)
            except Exception:
                continue
    # Para cualquier lado no detectado en este frame, registramos ausencia en los historiales
    for side in ("Left", "Right"):
        if not detected_sides_current.get(side):
            try:
                h = dedos_detectados_historial.get(side)
                h.append(None)
                if len(h) > HISTORIAL_SIZE:
                    h.pop(0)
            except Exception:
                pass
            try:
                for d in TODOS_LOS_DEDOS:
                    ph = dedos_presencia_historial[side][d]
                    ph.append(False)
                    if len(ph) > HISTORIAL_SIZE:
                        ph.pop(0)
            except Exception:
                pass

    # --- LÓGICA DE JUEGO ---
    dedo_req_izq = COLOR_A_DEDO.get(color_obj_izq)
    dedo_req_der = COLOR_A_DEDO.get(color_obj_der)

    # Calcular dedo estable por mano usando la moda del historial
    def dedo_estable(hist):
        if not hist:
            return None
        c = Counter([h for h in hist if h is not None])
        if not c:
            return None
        dedo, cnt = c.most_common(1)[0]
        # Requerir que la moda aparezca al menos en la mitad de la ventana para ser fiable
        if cnt >= max(1, len(hist)//2 + 1):
            return dedo
        return None

    def presencia_estable(hist_bool):
        if not hist_bool:
            return False
        # Contar True en la ventana; requerir mayoría
        trues = sum(1 for v in hist_bool if v)
        return trues >= max(1, len(hist_bool)//2 + 1)

    dedo_estable_izq = dedo_estable(dedos_detectados_historial["Left"])
    dedo_estable_der = dedo_estable(dedos_detectados_historial["Right"])

    # Validación primaria: usar dedos detectados en el frame (comportamiento similar a DedosArriba)
    cur_set_izq = dedos_actuales_sets.get("Left") or set()
    cur_set_der = dedos_actuales_sets.get("Right") or set()

    cur_ok_izq = (dedo_req_izq in cur_set_izq)
    cur_ok_der = (dedo_req_der in cur_set_der)

    # Comprobar presencia estable del dedo requerido como respaldo
    dedo_presente_izq = False
    dedo_presente_der = False
    if dedo_req_izq in TODOS_LOS_DEDOS:
        dedo_presente_izq = presencia_estable(dedos_presencia_historial["Left"][dedo_req_izq])
    if dedo_req_der in TODOS_LOS_DEDOS:
        dedo_presente_der = presencia_estable(dedos_presencia_historial["Right"][dedo_req_der])

    # Aceptar si cur_set lo indica, o si la moda/presencia lo respalda
    postura_izquierda_correcta = cur_ok_izq or (dedo_estable_izq == dedo_req_izq) or dedo_presente_izq
    postura_derecha_correcta = cur_ok_der or (dedo_estable_der == dedo_req_der) or dedo_presente_der

    # --- DIBUJAR INDICADORES LATERALES (qué dedo por color debe mantenerse arriba) ---
    try:
        # Top circular indicators removed — usamos las columnas laterales en su lugar
        # Dibujar columnas laterales con la secuencia de colores (5 por lado)
        try:
            cx_izq = 140
            cx_der = 1140
            y_start = 140
            separacion_y = 95
            cv2.putText(dashboard, f"Ronda {ronda_actual}/{TOTAL_RONDAS}", (560, 80), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (30, 30, 30), 2)
            cv2.putText(dashboard, f" {dedo_req_izq}", (cx_izq - 70, 85), cv2.FONT_HERSHEY_SIMPLEX, 0.75, (30, 30, 30), 2)
            cv2.putText(dashboard, f" {dedo_req_der}", (cx_der - 70, 85), cv2.FONT_HERSHEY_SIMPLEX, 0.75, (30, 30, 30), 2)
            for i in range(5):
                cy = y_start + i * separacion_y
                # izquierda
                col_i = COLORES.get(secuencia_izquierda[i], (200,200,200))
                # si la ronda ya pasó, mostrar gris
                if i < indice_actual:
                    fill_i = (200,200,200)
                else:
                    fill_i = col_i
                cv2.circle(dashboard, (cx_izq, cy), 35, fill_i, -1)
                # contorno amarillo si es la ronda actual
                if i == indice_actual:
                    cv2.circle(dashboard, (cx_izq, cy), 43, (0,255,255), 4)

                # derecha
                col_d = COLORES.get(secuencia_derecha[i], (200,200,200))
                if i < indice_actual:
                    fill_d = (200,200,200)
                else:
                    fill_d = col_d
                cv2.circle(dashboard, (cx_der, cy), 35, fill_d, -1)
                if i == indice_actual:
                    cv2.circle(dashboard, (cx_der, cy), 43, (0,255,255), 4)
        except Exception:
            pass
    except Exception:
        pass

    if postura_izquierda_correcta and postura_derecha_correcta:
        if not ambos_correctos_sostenido:
            ambos_correctos_sostenido = True
            tiempo_inicio_acierto = time.time()
        else:
            tiempo_transcurrido = time.time() - tiempo_inicio_acierto
            progreso = int((tiempo_transcurrido / TIEMPO_REQUERIDO) * 100)
            cv2.putText(frame, f"Sosten: {progreso}%", (50, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 3)
            
            if tiempo_transcurrido >= TIEMPO_REQUERIDO:
                aciertos += 1
                indice_actual += 1
                ambos_correctos_sostenido = False
                tiempo_inicio_acierto = 0
                dedos_detectados_historial["Left"].clear()
                dedos_detectados_historial["Right"].clear()
                if indice_actual >= ELEMENTOS_POR_RONDA:
                    if ronda_actual < TOTAL_RONDAS:
                        ronda_actual += 1
                        indice_actual = 0
                        secuencia_izquierda = crear_secuencia()
                        secuencia_derecha = crear_secuencia()
                        dedos_detectados_historial["Left"].clear()
                        dedos_detectados_historial["Right"].clear()
                        for side in ("Left", "Right"):
                            for d in TODOS_LOS_DEDOS:
                                dedos_presencia_historial[side][d].clear()
                    else:
                        ronda_actual += 1
    else:
        tiempo_actual = time.time()
        mano_izquierda_presente = dedos_detectados.get("Left") is not None
        mano_derecha_presente = dedos_detectados.get("Right") is not None
        postura_izquierda_incorrecta = mano_izquierda_presente and not postura_izquierda_correcta
        postura_derecha_incorrecta = mano_derecha_presente and not postura_derecha_correcta

        if postura_izquierda_incorrecta or postura_derecha_incorrecta:
            if tiempo_actual - ultimo_tiempo_error > 1.0:
                errores += 1
                ultimo_tiempo_error = tiempo_actual
        ambos_correctos_sostenido = False

    # --- DIBUJAR EL DASHBOARD ---
    # 1. Pegar la cámara en el centro superior (Redimensionada a 800x600)
    frame_redimensionado = cv2.resize(frame, (800, 600))
    dashboard[20:620, 240:1040] = frame_redimensionado

    # Zona inferior limpia: se omiten los indicadores inferiores para mantener el área bajo la cámara despejada

    # Desplegar la ventana armada
    cv2.imshow("Actividad: Gimnasia Bimanual", dashboard)
    
    if cv2.waitKey(10) & 0xFF == 27: # Salida limpia con ESC
        break

cap.release()
cv2.destroyAllWindows()
RESULTADO_PATH.parent.mkdir(parents=True, exist_ok=True)
RESULTADO_PATH.write_text(f"RESULTADO:{aciertos},{errores}", encoding="utf-8")
sys.exit(0)