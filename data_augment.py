from PIL import Image,ImageEnhance
from scipy.misc import imsave
import time
import numpy as np
import imutils
import sys
import argparse
import os
import openface
import socket

HOST = "localhost"
PORT = 8080
#sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
#sock.connect((HOST, PORT))



os.chdir('/home/ayush/PycharmProjects/face_reco/openface-master/')
print(os.getcwd())
#sock.sendall(str(os.getcwd())+'\n')
fileDir = os.path.dirname(os.path.realpath(__file__))
modelDir = os.path.join(os.getcwd(), 'models')
dlibModelDir = os.path.join(modelDir, 'dlib')
openfaceModelDir = os.path.join(modelDir, 'openface')



def augment(args) :


    landmarkMap = {
        'outerEyesAndNose': openface.AlignDlib.OUTER_EYES_AND_NOSE,
        'innerEyesAndBottomLip': openface.AlignDlib.INNER_EYES_AND_BOTTOM_LIP
    }

    if args.landmarks not in landmarkMap:
        raise Exception("Landmarks unrecognized: {}".format(args.landmarks))

    landmarkIndices = landmarkMap[args.landmarks]
    align = openface.AlignDlib(args.dlibFacePredictor)

    for subdir, dirs, files in os.walk(args.inputDir):
        for path in files:
            (imageClass, fName) = (os.path.basename(subdir), path)
            img = fName
            if img.endswith(".png") or img.endswith(".jpg"):
                #s = os.path.normpath(args.inputDir)+'/'+imageClass+'/'+img
                s = os.path.normpath(args.inputDir)+'/'+img
                print(s)
                im = Image.open(s)
                contrast = ImageEnhance.Contrast(im)
                bright = ImageEnhance.Brightness(im)
                j = 0
                i = 0.5
                l = 0.5
                for k in range(1,6,1):
                    contrast1 = contrast.enhance(i)
                    #print(type(contrast1))
                    bright1 = bright.enhance(l)
                    contrast1 = imutils.rotate_bound(np.array(contrast1),0)
                    bright1 = imutils.rotate_bound(np.array(bright1),0)

                    '''
                    start = time.time()

                    contrast1 = align.align(96, contrast,
                         landmarkIndices=landmarkIndices,
                         skipMulti=args.skipMulti)

                    bright1 = align.align(96, bright,
                           landmarkIndices=landmarkIndices,
                           skipMulti=args.skipMulti)
                    print('time taken {} '.format(time.time()-start))
                    if contrast1 is None:
                        print('unable to align'+img)
                        contrast = imutils.rotate_bound(np.array(contrast), 90)

                    if bright1 is None:
                        print('unable to align' + img)
                        bright = imutils.rotate_bound(np.array(bright), 90)'''
                    #name = os.path.normpath(args.inputDir)+'/'+imageClass+'/'+img.split('.')[0]
                    name = os.path.normpath(args.inputDir) + '/' + img.split('.')[0]
                    imsave(name+"_"+str(j)+".png",contrast1)
                    imsave(name+"_"+str(j+1)+".png",bright1)
                    j += 2
                    i += 0.5
                    l += 0.5
                    #sock.sendall('two saved\n')

if __name__ == '__main__':


    sys.argv = "xyz ./aligned-images/1  outerEyesAndNose".split()
    parser = argparse.ArgumentParser()
    parser.add_argument('inputDir', type=str, help="Input image directory.")
    parser.add_argument('--dlibFacePredictor', type=str, help="Path to dlib's face predictor.",
                        default=os.path.join(dlibModelDir, "shape_predictor_68_face_landmarks.dat"))
    parser.add_argument('landmarks', type=str,
                                 choices=['outerEyesAndNose',
                                          'innerEyesAndBottomLip',
                                          'eyes_1'],
                                 help='The landmarks to align to.')
    parser.add_argument(
        '--skipMulti', action='store_true', help="Skip images with more than one face.")
    args = parser.parse_args()

    #sock.sendall('started\n')
    augment(args)
    #sock.sendall('Bye\n')