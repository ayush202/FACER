import cv2
import os
import sys
import argparse
import shutil
import openface
import socket

HOST = "localhost"
PORT = 8080
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((HOST, PORT))

fileDir = os.path.dirname(os.path.realpath(__file__))
modelDir = os.path.join(fileDir, '..', 'models')
dlibModelDir = os.path.join(modelDir, 'dlib')
openfaceModelDir = os.path.join(modelDir, 'openface')


def create(args) :
    openface.helper.mkdirP(args.outDir)
    check = os.listdir(os.path.normpath(args.outDir)).__contains__(args.name)
    print(check)
    sock.sendall((str(check)+"\n").encode())
    if not check:
        os.mkdir(os.path.join(args.outDir,args.name))
    else:
        shutil.rmtree(os.path.join(args.outDir, args.name))
        os.mkdir(os.path.join(args.outDir, args.name))
    print('directory created')
    sock.sendall('directory created\n'.encode())
    video_capture = cv2.VideoCapture(args.captureDevice)
    video_capture.set(3, args.width)
    video_capture.set(4, args.height)
    i=0
    while True:
        ret, frame = video_capture.read()
        rgbImg = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        bb = align.getAllFaceBoundingBoxes(rgbImg)
        if len(bb) > 1:
            print('Only one face allowed in frame')
            continue
        alignedFaces = []
        for box in bb:
            alignedFaces.append(align.align(args.imgDim,
                    rgbImg,box,
                landmarkIndices=openface.AlignDlib.OUTER_EYES_AND_NOSE))
        if len(alignedFaces) == 0:
            print("Unable to align the frame"+"\n")
            sock.sendall('Unable to align the frame \n'.encode())
        else:
            s = args.outDir+str(args.name)+'/'+str(i)+'.png'
            sock.sendall('saving image\n'.encode())
            print(s+"\n")
            sock.sendall((s+"\n").encode())
            rgbImg = cv2.cvtColor(alignedFaces[0], cv2.COLOR_BGR2RGB)
            cv2.imwrite(s, rgbImg)

            i += 1
            if i == 20:
                break
            cv2.rectangle(frame, (bb[0].left(), bb[0].top()), (bb[0].right(), bb[0].bottom()), (120, 255, 150), 2)
            cv2.putText(frame,"Align your face in the rectangle.",
                        (70,20), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (115, 50, 240), 1)
            cv2.imshow('', frame)
        if cv2.waitKey(1) & 0xFF == ord('q') & i >= 20:
            break
    video_capture.release()
    cv2.destroyAllWindows()

if __name__ == '__main__':

    os.chdir('..')
    #sys.argv = "xyz --captureDevice 0 --width 500 --height 500  --outDir /home/ayush/PycharmProjects/face_recognition/openface-master/aligned-images/ --name xyz".split()

    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--dlibFacePredictor',
        type=str,
        help="Path to dlib's face predictor.",
        default=os.path.join(
            dlibModelDir,
            "shape_predictor_68_face_landmarks.dat"))
    parser.add_argument('--imgDim', type=int,
                        help="Default image dimension.", default=96)
    parser.add_argument(
        '--captureDevice',
        type=int,
        default=0,
        help='Capture device. 0 for latop webcam and 1 for usb webcam')
    parser.add_argument('--width', type=int, default=320)
    parser.add_argument('--height', type=int, default=240)
    parser.add_argument('--outDir', type=str, help='path to the saved image')
    parser.add_argument('--name', type=str,help='name of the person')
    parser.add_argument('--threshold', type=float, default=0.5)
    parser.add_argument('--cuda', action='store_true')
    parser.add_argument('--verbose', action='store_true')

    args = parser.parse_args()

    align = openface.AlignDlib(args.dlibFacePredictor)
    print('started')
    sock.sendall('started \n'.encode())
    sock.sendall(str(sys.argv).encode())
    create(args)
    sock.sendall("Bye\n".encode())
    sock.close()
