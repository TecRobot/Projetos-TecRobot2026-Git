import cv2
import apriltag
import numpy as np
from networktables import NetworkTables

# ==============================
# CONFIGURAÇÕES
# ==============================

# ⚠️ SUBSTITUA pelos valores calibrados da sua câmera
CAMERA_MATRIX = np.array([
    [1000, 0, 320],
    [0, 1000, 240],
    [0, 0, 1]
], dtype=np.float32)

DIST_COEFFS = np.zeros((5, 1))

TAG_SIZE = 0.165  # 6.5 polegadas em metros (FRC padrão)

ROBORIO_IP = "10.XX.XX.2"  # Substitua pelo IP real do seu roboRIO

# ==============================
# NETWORKTABLES
# ==============================

NetworkTables.initialize(server=ROBORIO_IP)
nt = NetworkTables.getTable("Vision")

# ==============================
# INICIALIZAÇÃO DA CÂMERA
# ==============================

def initialize_camera():
    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        raise RuntimeError("Erro ao abrir a câmera.")

    cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
    return cap

# ==============================
# DETECTOR APRILTAG
# ==============================

def initialize_detector():
    options = apriltag.DetectorOptions(families="tag36h11")
    return apriltag.Detector(options)

# ==============================
# DETECÇÃO
# ==============================

def detect_tags(frame, detector):
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    detections = detector.detect(gray)
    return detections

# ==============================
# ESTIMATIVA DE POSE
# ==============================

def estimate_pose(detection):
    # Pontos 3D da tag no mundo real
    obj_points = np.array([
        [-TAG_SIZE/2, -TAG_SIZE/2, 0],
        [ TAG_SIZE/2, -TAG_SIZE/2, 0],
        [ TAG_SIZE/2,  TAG_SIZE/2, 0],
        [-TAG_SIZE/2,  TAG_SIZE/2, 0]
    ], dtype=np.float32)

    img_points = detection.corners.astype(np.float32)

    success, rvec, tvec = cv2.solvePnP(
        obj_points,
        img_points,
        CAMERA_MATRIX,
        DIST_COEFFS
    )

    if success:
        distance = np.linalg.norm(tvec)
        return rvec, tvec, distance

    return None, None, None

# ==============================
# DESENHO NA TELA
# ==============================

def draw_detections(frame, detections):
    for detection in detections:

        corners = detection.corners.astype(int)
        cv2.polylines(frame, [corners], True, (0,255,0), 2)

        center = detection.center.astype(int)
        cv2.circle(frame, tuple(center), 5, (255,0,0), -1)

        tag_id = detection.tag_id

        rvec, tvec, distance = estimate_pose(detection)

        cv2.putText(frame, f"ID: {tag_id}",
                    (center[0]-20, center[1]-40),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255,255,255), 2)

        if distance is not None:
            cv2.putText(frame, f"Dist: {distance:.2f} m",
                        (center[0]-20, center[1]-20),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255,255,255), 2)

# ==============================
# ENVIO PARA O ROBO
# ==============================

def send_to_robot(detections):
    if len(detections) > 0:
        detection = detections[0]
        tag_id = detection.tag_id
        _, tvec, distance = estimate_pose(detection)

        nt.putNumber("TagID", tag_id)
        nt.putNumber("Distance", distance if distance else -1)
        nt.putNumber("X", tvec[0][0] if tvec is not None else 0)
        nt.putNumber("Y", tvec[1][0] if tvec is not None else 0)
        nt.putNumber("Z", tvec[2][0] if tvec is not None else 0)
    else:
        nt.putNumber("TagID", -1)

# ==============================
# LOOP PRINCIPAL
# ==============================

def main():

    cap = initialize_camera()
    detector = initialize_detector()

    print("Sistema de visão iniciado.")

    while True:
        ret, frame = cap.read()
        if not ret:
            print("Erro ao capturar frame.")
            break

        detections = detect_tags(frame, detector)

        draw_detections(frame, detections)
        send_to_robot(detections)

        cv2.imshow("FRC AprilTag Vision", frame)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()



#Este código é um exemplo de um sistema de visão para FRC usando Python, OpenCV e apriltag. Ele captura vídeo da câmera, detecta AprilTags, estima a pose e envia os dados para o roboRIO via NetworkTables.
#from photonlibpy.photonCamera import PhotonCamera
#import time

#class VisionSystem:
#    def __init__(self, camera_name):
#        # O nome deve ser o mesmo configurado na interface web do PhotonVision
#        self.camera = PhotonCamera(camera_name)
#        self.target_id_desejado = None
#
#    def buscar_tag_especifica(self, id_alvo):
#        self.target_id_desejado = id_alvo
#        
#        # Obtém o resultado mais recente da pipeline
#        result = self.camera.getLatestResult()
#
#        if result.hasTargets():
#            # Pegamos a lista de todos os alvos visíveis
#            targets = result.getTargets()
#            
#            for target in targets:
#                if target.getFiducialId() == self.target_id_desejado:
#                    # ACHOU A TAG CERTA!
                  #  print(f"--- Tag {id_alvo} Detectada ---")
                    
                    # Dados de posição (Pitch, Yaw e Area)
                 #   yaw = target.getYaw()     # Erro lateral (em graus)
                 #   pitch = target.getPitch() # Erro vertical (em graus)
                    
                    # Se você configurou a calibração 3D:
                #    transform = target.getBestCameraToTarget()
                #    distancia_x = transform.x
                    
                 #   self.executar_movimento(yaw, distancia_x)
                 #   return True # Encontrou e processou
            
#            print(f"Vendo outras tags, mas não a {id_alvo}...")
#        else:
#            print("Nenhuma tag na visão.")
        
#        return False

#    def executar_movimento(self, erro_yaw, distancia):
        # Lógica simples de exemplo:
#        if abs(erro_yaw) > 2.0:
#            direcao = "Direita" if erro_yaw > 0 else "Esquerda"
#            print(f"=> Girando para a {direcao} (Erro: {erro_yaw:.2f}°)")
#        else:
#            print(f"=> Alinhado! Distância: {distancia:.2f}m")

# --- Exemplo de Uso ---
#vision = VisionSystem("camera_raspberry")

#while True:
    # Exemplo: Você pode mudar o ID aqui baseado em uma tecla ou sensor
#    id_para_buscar = 13 
#    
#    vision.buscar_tag_especifica(id_para_buscar)
#    
#    time.sleep(0.1) # Pequena pausa para não sobrecarregar o processador


    