import cv2
import sys
import random
import time
import numpy as np

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
DEBUG_CAPTURA = True

secuencia_izquierda = [random.choice(LISTA_COLORES) for _ in range(5)]
secuencia_derecha = [random.choice(LISTA_COLORES) for _ in range(5)]
indice_actual = 0

aciertos = 0
errores = 0
ambos_correctos_sostenido = False
tiempo_inicio_acierto = 0
TIEMPO_REQUERIDO = 1.0  
ultimo_tiempo_error = 0


# --- FUNCIÓN ROBUSTA DE CÁMARA ---
def open_working_camera(max_index=5):
    backends = [("ANY", cv2.CAP_ANY), ("MSMF", cv2.CAP_MSMF), ("DSHOW", cv2.CAP_DSHOW)]
    for name, backend in backends:
        for i in range(max_index):
            cap = cv2.VideoCapture(i, backend)
            if not cap or not cap.isOpened():
                try: cap.release()
                except Exception: pass
                continue
            ret, frame = cap.read()
            if ret and frame is not None:
                h, w = frame.shape[:2]
                print(f"CAMERA_OPENED:{i},{name},{w}x{h}", flush=True)
                return cap
            try: cap.release()
            except Exception: pass
    print("CAMERA_OPENED:None", flush=True)
    return None


def detectar_dedos_levantados(puntos, mano_lado):
    margen = 0.04
    margen_vertical = 0.05

    dedos_arriba = {}
    if mano_lado == "Left":
        dedos_arriba["Pulgar"] = puntos[4].x > (puntos[3].x + margen)
    else:
        dedos_arriba["Pulgar"] = puntos[4].x < (puntos[3].x - margen)

    dedos_arriba["Indice"] = puntos[8].y < (puntos[6].y - margen_vertical)
    dedos_arriba["Medio"] = puntos[12].y < (puntos[10].y - margen_vertical)
    dedos_arriba["Anular"] = puntos[16].y < (puntos[14].y - margen_vertical)
    dedos_arriba["Menique"] = puntos[20].y < (puntos[18].y - margen_vertical)

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

    candidatos = [(dedo, score) for dedo, score in scores.items() if score > 0.03]
    if not candidatos:
        return None, scores

    candidatos.sort(key=lambda item: item[1], reverse=True)
    dedo_dominante, score_dominante = candidatos[0]
    segundo_score = candidatos[1][1] if len(candidatos) > 1 else 0

    if score_dominante < 0.03 or (score_dominante - segundo_score) < 0.02:
        return None, scores

    return dedo_dominante, scores


def texto_dedos(dedos):
    return "-" if not dedos else ",".join(sorted(dedos))

print("Inicializando componentes...", flush=True)
cap = open_working_camera(6)

if cap is None:
    print("Error: no se pudo abrir ninguna cámara.", flush=True)
    sys.exit(1)

# Creamos la ventana nativa de OpenCV
cv2.namedWindow("Actividad: Gimnasia Bimanual", cv2.WINDOW_NORMAL)
cv2.resizeWindow("Actividad: Gimnasia Bimanual", 1280, 720)

while cap.isOpened():
    success, frame = cap.read()
    if not success: break

    # Lienzo gris claro para el Dashboard de fondo
    dashboard = np.full((720, 1280, 3), 240, dtype=np.uint8) 

    if indice_actual >= 5:
        break
        
    color_obj_izq = secuencia_izquierda[indice_actual]
    color_obj_der = secuencia_derecha[indice_actual]
    
    # --- PROCESAMIENTO MEDIA PIPE ---
    frame = cv2.flip(frame, 1)  
    rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    results = hands.process(rgb_frame)

    alto_cam, ancho_cam, _ = frame.shape
    dedos_detectados = {"Left": None, "Right": None}

    if results.multi_hand_landmarks and results.multi_handedness:
        for idx, hand_handedness in enumerate(results.multi_handedness):
            if idx >= len(results.multi_hand_landmarks): continue
            try:
                detected_label = hand_handedness.classification[0].label  
                mano_lado = "Left" if detected_label == "Right" else "Right"
                
                hand_landmarks = results.multi_hand_landmarks[idx]
                mp_drawing.draw_landmarks(frame, hand_landmarks, mp_hands.HAND_CONNECTIONS)
                
                puntos = hand_landmarks.landmark
                if len(puntos) < 21: continue
                
                dedos_actuales, dedos_arriba = detectar_dedos_levantados(puntos, mano_lado)
                dedo_dominante, scores = obtener_dedo_dominante(puntos, mano_lado)

                for d_name, d_id in DEDOS_IDS.items():
                    px, py = int(puntos[d_id].x * ancho_cam), int(puntos[d_id].y * alto_cam)
                    color_nombre = DEDO_A_COLOR.get(d_name, "Azul")
                    radio = 12 if dedos_arriba.get(d_name) else 6
                    cv2.circle(frame, (px, py), radio, COLORES[color_nombre], -1)

                dedos_detectados[mano_lado] = [dedo_dominante] if dedo_dominante else []
            except Exception:
                continue

    # --- LÓGICA DE JUEGO ---
    dedo_req_izq = COLOR_A_DEDO.get(color_obj_izq)
    dedo_req_der = COLOR_A_DEDO.get(color_obj_der)
    lista_izq = dedos_detectados.get("Left") or []
    lista_der = dedos_detectados.get("Right") or []

    postura_izquierda_correcta = lista_izq == [dedo_req_izq]
    postura_derecha_correcta = lista_der == [dedo_req_der]

    if DEBUG_CAPTURA:
        print(f"[DEBUG] target L={dedo_req_izq} R={dedo_req_der} | L={texto_dedos(lista_izq)} R={texto_dedos(lista_der)} | okL={postura_izquierda_correcta} okR={postura_derecha_correcta}", flush=True)

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
    else:
        tiempo_actual = time.time()
        mano_izquierda_presente = len(lista_izq) > 0
        mano_derecha_presente = len(lista_der) > 0
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

    # 2. Dibujar la secuencia de aciertos abajo
    espacio_x = 120
    inicio_x = 380
    y_centro = 670
    
    cv2.putText(dashboard, "Secuencia:", (150, y_centro + 10), cv2.FONT_HERSHEY_SIMPLEX, 1, (0,0,0), 2)

    for i in range(5):
        x_pos = inicio_x + (i * espacio_x)
        color_circulo = COLORES[secuencia_derecha[i]]
        
        cv2.circle(dashboard, (x_pos, y_centro), 30, color_circulo, -1)
        
        # CONTORNO VERDE INDICADOR DE TURNO ACTUAL
        if i == indice_actual:
            cv2.circle(dashboard, (x_pos, y_centro), 38, (0, 255, 0), 5) 

    if DEBUG_CAPTURA:
        cv2.putText(dashboard, f"L: {texto_dedos(lista_izq)}", (850, 645), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 0), 2)
        cv2.putText(dashboard, f"R: {texto_dedos(lista_der)}", (850, 675), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 0), 2)
        cv2.putText(dashboard, f"OK L:{postura_izquierda_correcta} R:{postura_derecha_correcta}", (850, 705), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 0), 2)

    # Desplegar la ventana armada
    cv2.imshow("Actividad: Gimnasia Bimanual", dashboard)
    
    if cv2.waitKey(10) & 0xFF == 27: # Salida limpia con ESC
        break

cap.release()
cv2.destroyAllWindows()
print(f"RESULTADO:{aciertos},{errores}", flush=True)
sys.exit(0)