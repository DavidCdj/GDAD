import cv2

names = [
    'Integrated Camera',
    'video=Integrated Camera',
]

for name in names:
    print(f'Testing {name!r}')
    for backend_name, backend in [('ANY', cv2.CAP_ANY), ('MSMF', cv2.CAP_MSMF), ('DSHOW', cv2.CAP_DSHOW)]:
        cap = cv2.VideoCapture(name, backend)
        opened = cap.isOpened()
        print(f'  backend={backend_name} opened={opened}')
        if opened:
            for _ in range(10):
                ret, frame = cap.read()
                if ret and frame is not None:
                    print(f'  backend={backend_name} read_ok=True shape={frame.shape}')
                    break
            else:
                print(f'  backend={backend_name} read_ok=False')
        cap.release()
