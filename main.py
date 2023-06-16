import torch

# 모델 파일 경로
model_path = '/content/drive/MyDrive/best.pt'

# 모델 로드
model = torch.load(model_path)
# model = torch.load(model_path,map_location=torch.device('cpu'))
# model = YOLO("/content/yolov8l.pt")

# tensorRT로 변환
# model.export(format='engine', device='cpu', half=True)
model = model['model']
model = model.float()

model.eval()

# 디바이스 설정
# device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
device = torch.device("cpu")

# 모델을 지정한 디바이스로 이동
model = model.to(device)


# trace 방식
model_input = torch.rand(1,3, 64, 64)
traced = torch.jit.trace(model, (model_input,))
traced.save("/content/drive/MyDrive/model.pt")