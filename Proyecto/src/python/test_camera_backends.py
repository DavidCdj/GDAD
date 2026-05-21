import cv2

backends = [
    ('ANY', cv2.CAP_ANY),
    ('DSHOW', cv2.CAP_DSHOW),
    ('MSMF', cv2.CAP_MSMF)
]

print('Testing indices 0-7 with backends ANY, DSHOW, MSMF')
for name, backend in backends:
    print(f'--- Backend {name} ({backend}) ---')
    for i in range(8):
        cap = cv2.VideoCapture(i, backend)
        opened = cap.isOpened()
        print(f'Index {i}: opened={opened}')
        if opened:
            ret, frame = cap.read()
            print(f'  read_ok={ret and frame is not None}')
        cap.release()
print('Done')
