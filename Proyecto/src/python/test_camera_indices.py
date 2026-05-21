import cv2

print('Starting camera index test')
for i in range(8):
    print(f'--- Testing index {i} ---')
    cap = cv2.VideoCapture(i, cv2.CAP_DSHOW)
    opened = cap.isOpened()
    print(f'opened: {opened}')
    read_ok = False
    if opened:
        ret, frame = cap.read()
        read_ok = bool(ret and frame is not None)
        print(f'read_ok: {read_ok}')
        if read_ok:
            h, w = frame.shape[:2]
            print(f'frame size: {w}x{h}')
    cap.release()
print('Done')
