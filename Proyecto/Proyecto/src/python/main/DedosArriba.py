import cv2
import sys
import random
import time
import numpy as np
import mediapipe as mp
import math
from PIL import Image, ImageSequence

sys.stdout.reconfigure(line_buffering=True)

mp_hands = mp.solutions.hands
mp_drawing = mp.solutions.drawing_utils

hands = mp_hands.Hands(
    static_image_mode=False,
    max_num_hands=2,  
    min_detection_confidence=0.7,
    min_tracking_confidence=0.7
)

# --- CONFIGURACIÓN DE LAS SECUENCIAS (Tu Imagen) ---
# Formato: (Cantidad Izquierda, Cantidad Derecha)
SECUENCIA_VERDE = [(random.randint(1, 5), random.randint(1, 5)), (random.randint(1, 5), random.randint(1, 5)), (random.randint(1, 5), random.randint(1, 5)), (random.randint(1, 5), random.randint(1, 5)), (random.randint(1, 5), random.randint(1, 5))]
SECUENCIA_AZUL = [(random.randint(1, 5), random.randint(1, 5)), (random.randint(1, 5), random.randint(1, 5)), (random.randint(1, 5), random.randint(1, 5)), (random.randint(1, 5), random.randint(1, 5)), (random.randint(1, 5), random.randint(1, 5))]

# Definición de colores en formato BGR para los círculos
COLOR_VERDE_CLARO = (100, 255, 100)
COLOR_VERDE_OSCURO = (100, 150, 50)
COLOR_AZUL_OSCURO = (200, 50, 50)
COLOR_AZUL_CLARO = (255, 150, 50)
COLOR_BLANCO = (255, 255, 255)
COLOR_NEGRO = (0, 0, 0)
COLOR_INDICADOR = (0, 255, 255) # Amarillo brillante para el contorno de turno

# Estados del juego
fase_actual = "VERDE"  # Puede cambiar a "AZUL"
indice_ronda = 0       # Va del 0 al 4 en cada fase

aciertos_totales = 0
errores_totales = 0
pose_correcta_sostenido = False 
tiempo_inicio_acierto = 0
TIEMPO_REQUERIDO = 1.0  
ultimo_tiempo_error = 0

# --- HISTORIAL PARA SUAVIZADO ---
historial_izq = []
historial_der = []
HISTORIAL_SIZE = 3

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
                return cap
            try: cap.release()
            except Exception: pass
    return None

cap = open_working_camera(6)
if cap is None:
    print("Error: no se pudo abrir la cámara.", flush=True)
    sys.exit(1)

cv2.namedWindow("Actividad: Conteo de Dedos", cv2.WINDOW_NORMAL)
cv2.resizeWindow("Actividad: Conteo de Dedos", 1280, 720)



while cap.isOpened():
    success, frame = cap.read()
    if not success: break

    dashboard = np.full((720, 1280, 3), 245, dtype=np.uint8)

    # Definir qué datos usar según la fase
    lista_rondas = SECUENCIA_VERDE if fase_actual == "VERDE" else SECUENCIA_AZUL
    
    # Si terminamos la fase azul, cerramos el programa
    if fase_actual == "TERMINADO" or indice_ronda >= 5:
        break

    req_izq, req_der = lista_rondas[indice_ronda]

    frame = cv2.flip(frame, 1)
    rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    results = hands.process(rgb_frame)

    alto_cam, ancho_cam, _ = frame.shape
    
    # Conteo instantáneo de este frame
    cuenta_izq_inst = 0
    cuenta_der_inst = 0

    if results.multi_hand_landmarks and results.multi_handedness:
        for idx, hand_handedness in enumerate(results.multi_handedness):
            if idx >= len(results.multi_hand_landmarks): continue
            try:
                detected_label = hand_handedness.classification[0].label
                mano_lado = "Right" if detected_label == "Right" else "Left"
                hand_landmarks = results.multi_hand_landmarks[idx]
                
                mp_drawing.draw_landmarks(frame, hand_landmarks, mp_hands.HAND_CONNECTIONS)
                puntos = hand_landmarks.landmark

                margen = 0.04
                margen_vertical = 0.02

                dx = puntos[4].x - puntos[17].x
                dy = puntos[4].y - puntos[17].y
                distancia_pulgar = math.sqrt(dx**2 + dy**2)
                pulgar_abierto = distancia_pulgar > 0.13
                dedos_estado = {
                    #"Pulgar": puntos[4].x > (puntos[3].x + margen) if mano_lado == "Left" else puntos[4].x < (puntos[3].x - margen),
                    "Pulgar": pulgar_abierto,
                    "Indice": puntos[8].y < (puntos[6].y - margen_vertical),
                    "Medio": puntos[12].y < (puntos[10].y - margen_vertical),
                    "Anular": puntos[16].y < (puntos[14].y - margen_vertical),
                    "Menique": puntos[20].y < (puntos[18].y - margen_vertical)
                }

                subtotal = sum(1 for status in dedos_estado.values() if status)
                if mano_lado == "Left":
                    cuenta_izq_inst = subtotal
                else:
                    cuenta_der_inst = subtotal

            except Exception:
                continue

    # --- APLICAR SUAVIZADO MEDIANTE HISTORIAL (Evita saltos bruscos) ---
    historial_izq.append(cuenta_izq_inst)
    historial_der.append(cuenta_der_inst)
    if len(historial_izq) > HISTORIAL_SIZE: historial_izq.pop(0)
    if len(historial_der) > HISTORIAL_SIZE: historial_der.pop(0)

    # La cuenta final es el promedio redondeado o la opción más repetida reciente
    dedos_final_izq = int(np.round(np.mean(historial_izq)))
    dedos_final_der = int(np.round(np.mean(historial_der)))

    # --- LÓGICA DE VALIDACIÓN ---
    if dedos_final_izq == req_izq and dedos_final_der == req_der:
        # CORREGIDO: quitamos la 'a' del final de la variable
        if not pose_correcta_sostenido: 
            pose_correcta_sostenido = True
            tiempo_inicio_acierto = time.time()
        else:
            tiempo_transcurrido = time.time() - tiempo_inicio_acierto
            progreso = int((tiempo_transcurrido / TIEMPO_REQUERIDO) * 100)
            cv2.putText(frame, f"Bien! Sosten: {progreso}%", (40, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 3)
            
            if tiempo_transcurrido >= TIEMPO_REQUERIDO:
                aciertos_totales += 1
                indice_ronda += 1
                pose_correcta_sostenido = False
                
                # Si completamos las 5 rondas de la fase verde, saltamos a la azul
                if indice_ronda >= 5 and fase_actual == "VERDE":
                    fase_actual = "AZUL"
                    indice_ronda = 0
                    historial_izq.clear()
                    historial_der.clear()
                elif indice_ronda >= 5 and fase_actual == "AZUL":
                    fase_actual = "TERMINADO"
    else:
        # Penalización suave por error
        tiempo_actual = time.time()
        if (dedos_final_izq != 0 and dedos_final_izq != req_izq) or (dedos_final_der != 0 and dedos_final_der != req_der):
            if tiempo_actual - ultimo_tiempo_error > 1.5:
                errores_totales += 1
                ultimo_tiempo_error = tiempo_actual
        pose_correcta_sostenido = False

    # --- MONTAJE DEL DASHBOARD (1280x720) ---
    # 1. Cámara en la parte superior izquierda
    frame_redim = cv2.resize(frame, (640, 480))
    dashboard[60:540, 320:960] = frame_redim

    # 2. Renderizar las columnas de la gimnasia en la parte derecha
    cx_izq = 140
    cx_der = 1140
    y_inicio = 140
    separacion_y = 95

    cv2.putText(dashboard, "MANO IZQ", (cx_izq - 60, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.8, COLOR_NEGRO, 2)
    cv2.putText(dashboard, "MANO DER", (cx_der - 65, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.8, COLOR_NEGRO, 2)
    
    # Texto de la Fase en la que se encuentra
    cv2.putText(dashboard, f"EJERCICIO: COLUMNAS {fase_actual}", (320, 580), cv2.FONT_HERSHEY_SIMPLEX, 1, COLOR_NEGRO, 3)
    cv2.putText(dashboard, f"Detectado -> Izq: {dedos_final_izq} | Der: {dedos_final_der}", (320, 630), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (100, 100, 100), 2)
    # Dibujar las 5 filas de círculos de la fase en juego
    for i in range(5):
        val_izq, val_der = lista_rondas[i]
        cy = y_inicio + (i * separacion_y)

        # Seleccionar la paleta de color según la fase activa
        if fase_actual == "VERDE":
            col_i = COLOR_VERDE_CLARO
            col_d = COLOR_VERDE_OSCURO
        else:
            col_i = COLOR_AZUL_OSCURO
            col_d = COLOR_AZUL_CLARO

        # Si la ronda ya pasó, la dejamos en un gris tenue para marcar que ya se completó
        if i < indice_ronda:
            col_i = (200, 200, 200)
            col_d = (180, 180, 180)

        # Dibujar los círculos rellenos
        cv2.circle(dashboard, (cx_izq, cy), 35, col_i, -1)
        cv2.circle(dashboard, (cx_der, cy), 35, col_d, -1)

        # Imprimir el número al centro de cada círculo
        cv2.putText(dashboard, str(val_izq), (cx_izq - 10, cy + 10), cv2.FONT_HERSHEY_SIMPLEX, 1.1, COLOR_BLANCO, 3)
        cv2.putText(dashboard, str(val_der), (cx_der - 10, cy + 10), cv2.FONT_HERSHEY_SIMPLEX, 1.1, COLOR_BLANCO, 3)

        # CONTORNO DE POSICIÓN ACTUAL: Resalta la fila completa que debe hacer ahorita
        if i == indice_ronda:
            cv2.circle(dashboard, (cx_izq, cy), 43, COLOR_INDICADOR, 4)
            cv2.circle(dashboard, (cx_der, cy), 43, COLOR_INDICADOR, 4)

    cv2.imshow("Actividad: Conteo de Dedos", dashboard)
    
    if cv2.waitKey(10) & 0xFF == 27:
        break

cap.release()
cv2.destroyAllWindows()

# Envío final a Java para Excel
print(f"RESULTADO:{aciertos_totales},{errores_totales}", flush=True)
sys.exit(0)