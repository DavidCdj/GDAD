import cv2

def test_indices(max_i=6):
    for i in range(max_i):
        cap = cv2.VideoCapture(i, cv2.CAP_DSHOW)  # usar CAP_DSHOW en Windows
        if not cap or not cap.isOpened():
            print(f"Índice {i}: no disponible")
            continue
        ret, frame = cap.read()
        if ret:
            print(f"Índice {i}: OK (mostrando 1 frame). Presiona una tecla para cerrar la ventana {i}")
            cv2.imshow(f"Cam {i}", frame)
            cv2.waitKey(0)
            cv2.destroyWindow(f"Cam {i}")
        else:
            print(f"Índice {i}: abierto pero sin frames")
        cap.release()

if __name__ == '__main__':
    test_indices(8)