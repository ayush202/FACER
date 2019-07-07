#!/usr/bin/env python2
#
# Copyright 2015-2016 Carnegie Mellon University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import argparse
import cv2
import numpy as np
import os
import sys
import random
import shutil

import openface
import openface.helper
from openface.data import iterImgs
from PIL import Image, ImageEnhance
import socket

HOST = "localhost"
PORT = 8080
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((HOST, PORT))


fileDir = os.path.dirname(os.path.realpath(__file__))
modelDir = os.path.join(fileDir, '..', 'models')
dlibModelDir = os.path.join(modelDir, 'dlib')
openfaceModelDir = os.path.join(modelDir, 'openface')


def write(vals, fName):
    if os.path.isfile(fName):
        print("{} exists. Backing up.".format(fName))
        sock.sendall("{} exists. Backing up.\n".format(fName).encode())
        os.rename(fName, "{}.bak".format(fName))
    with open(fName, 'w') as f:
        for p in vals:
            f.write(",".join(str(x) for x in p))
            f.write("\n")

def computeMeanMain(args):
    align = openface.AlignDlib(args.dlibFacePredictor)

    imgs = list(iterImgs(args.inputDir))
    if args.numImages > 0:
        imgs = random.sample(imgs, args.numImages)

    facePoints = []
    for img in imgs:
        rgb = img.getRGB()
        bb = align.getLargestFaceBoundingBox(rgb)
        alignedPoints = align.align(rgb, bb)
        if alignedPoints:
            facePoints.append(alignedPoints)

    facePointsNp = np.array(facePoints)
    mean = np.mean(facePointsNp, axis=0)
    std = np.std(facePointsNp, axis=0)

    write(mean, "{}/mean.csv".format(args.modelDir))
    write(std, "{}/std.csv".format(args.modelDir))

    # Only import in this mode.
    import matplotlib as mpl
    mpl.use('Agg')
    import matplotlib.pyplot as plt

    fig, ax = plt.subplots()
    ax.scatter(mean[:, 0], -mean[:, 1], color='k')
    ax.axis('equal')
    for i, p in enumerate(mean):
        ax.annotate(str(i), (p[0] + 0.005, -p[1] + 0.005), fontsize=8)
    plt.savefig("{}/mean.png".format(args.modelDir))


def alignMain(args):
    openface.helper.mkdirP(args.outDir)


    check = os.listdir(os.path.normpath(args.outDir)).__contains__(args.name)
    print(check)
    sock.sendall((str(check) + "\n").encode())
    if not check:
        os.mkdir(os.path.join(args.outDir, args.name))
    else:
        shutil.rmtree(os.path.join(args.outDir, args.name))
        os.mkdir(os.path.join(args.outDir, args.name))

    print('directory created')
    sock.sendall('directory created\n'.encode())
    imgs = list(iterImgs(args.inputDir))
    sock.sendall("length\n".encode())
    sock.sendall((str(len(imgs)*3)+"\n").encode())

    # Shuffle so multiple versions can be run at once.
    random.shuffle(imgs)

    landmarkMap = {
        'outerEyesAndNose': openface.AlignDlib.OUTER_EYES_AND_NOSE,
        'innerEyesAndBottomLip': openface.AlignDlib.INNER_EYES_AND_BOTTOM_LIP
    }
    if args.landmarks not in landmarkMap:
        raise Exception("Landmarks unrecognized: {}".format(args.landmarks))

    landmarkIndices = landmarkMap[args.landmarks]

    align = openface.AlignDlib(args.dlibFacePredictor)

    nFallbacks = 0
    cnt = 0
    for imgObject in imgs:
        print("=== {} ===".format(imgObject.path))
        s = os.path.normpath(args.inputDir)+'/'+imgObject.name
        img = cv2.imread(s)
        if img is None:
            sock.sendall("  + Unable to load.\n".encode())
            sock.send("processed\n".encode())
        width = img.shape[1]
        height = img.shape[0]
        check = False
        if width > 600:
            width = 600
            check = True
        if height > 600:
            height = 600
            check = True
        if check:
            img = cv2.resize(img, (width, height), interpolation=cv2.INTER_LINEAR)
        cv2.imwrite(os.path.normpath(args.inputDir)+'/1'+imgObject.name, img)
        im = Image.open(os.path.normpath(args.inputDir)+'/1'+imgObject.name)
        os.remove(os.path.normpath(args.inputDir)+'/1'+imgObject.name)
        contrast = ImageEnhance.Contrast(im)
        bright = ImageEnhance.Brightness(im)
        sock.sendall(("=== {} ===\n".format(imgObject.path)).encode())
        outDir = os.path.join(args.outDir, args.name)
        #outDir = os.path.join(args.outDir, imgObject.cls)
        openface.helper.mkdirP(outDir)
        outputPrefix = os.path.join(outDir, os.path.splitext(imgObject.name)[0])
        j = 0
        i = 0.7
        t = False
        for k in range(1,3,1):
            for l in range(1,4):
                if k != 1 and l == 1:
                    continue
                elif t:
                    im = Image.open(s)
                    contrast = ImageEnhance.Contrast(im)
                    bright = ImageEnhance.Brightness(im)
                if k == 1:
                    imgName = outputPrefix + ".png"
                elif l == 2:
                     imgObject = contrast.enhance(i)
                     imgObject = np.array(imgObject)
                     imgName = outputPrefix+"_"+str(j)+".png"
                elif l == 3:
                      imgObject = bright.enhance(i)
                      imgObject = np.array(imgObject)
                      imgName = outputPrefix+"_"+str(j+1)+".png"
                if k == 1:
                    rgb = imgObject.getRGB()
                    print(type(rgb))
                else:
                    rgb = imgObject
                cnt = cnt+1
                sock.sendall(("cnt "+str(cnt)+"\n").encode())
                if rgb is None:
                    if args.verbose:
                        print("  + Unable to load.")
                        sock.sendall("  + Unable to load.\n".encode())
                        sock.send("processed\n".encode())
                    outRgb = None
                else:
                    outRgb = align.align(args.size, rgb,
                                     landmarkIndices=landmarkIndices,
                                     skipMulti=args.skipMulti)
                    if outRgb is None and args.verbose:
                        print("  + Unable to align."+" "+str(k)+" "+str(l))
                        sock.sendall("  + Unable to align.\n".encode())
                        sock.send("processed\n".encode())
                if args.fallbackLfw and outRgb is None:
                    nFallbacks += 1
                    deepFunneled = "{}/{}.jpg".format(os.path.join(args.fallbackLfw,
                                                               imgObject.cls),
                                                  imgObject.name)
                    shutil.copy(deepFunneled, "{}/{}.jpg".format(os.path.join(args.outDir,
                                                                          imgObject.cls),
                                                             imgObject.name))

                if outRgb is not None:
                    if args.verbose:
                        print("  + Writing aligned file to disk.")
                        sock.sendall("  + Writing aligned file to disk.\n".encode())
                    outBgr = cv2.cvtColor(outRgb, cv2.COLOR_RGB2BGR)
                    cv2.imwrite(imgName, outBgr)
                    sock.sendall("processed\n".encode())
                    if k == 1:
                        x = outputPrefix+".png"
                        s = x
                        t = True
                        sock.sendall("write to index\n".encode())
                        sock.sendall((x+'\n').encode())
                        break
            if k != 1:
                i += 0.3
                j += 2
        if args.fallbackLfw:
            print('nFallbacks:', nFallbacks)

if __name__ == '__main__':

    #os.chdir('..')
    #sys.argv = "xyz ./training-images/aditya  --name 3 align outerEyesAndNose --outDir ./aligned-images/ --verbose".split()


    parser = argparse.ArgumentParser()

    parser.add_argument('inputDir', type=str, help="Input image directory.")
    parser.add_argument('--dlibFacePredictor', type=str, help="Path to dlib's face predictor.",
                        default=os.path.join(dlibModelDir, "shape_predictor_68_face_landmarks.dat"))
    parser.add_argument('--name', type=str, help='name of the person')

    subparsers = parser.add_subparsers(dest='mode', help="Mode")
    computeMeanParser = subparsers.add_parser(
        'computeMean', help='Compute the image mean of a directory of images.')
    computeMeanParser.add_argument('--numImages', type=int, help="The number of images. '0' for all images.",
                                   default=0)  # <= 0 ===> all imgs
    alignmentParser = subparsers.add_parser(
        'align', help='Align a directory of images.')
    alignmentParser.add_argument('landmarks', type=str,
                                 choices=['outerEyesAndNose',
                                          'innerEyesAndBottomLip',
                                          'eyes_1'],
                                 help='The landmarks to align to.')
    alignmentParser.add_argument(
        '--outDir', type=str, help="Output directory of aligned images.")
    alignmentParser.add_argument('--size', type=int, help="Default image size.",
                                 default=96)
    alignmentParser.add_argument('--fallbackLfw', type=str,
                                 help="If alignment doesn't work, fallback to copying the deep funneled version from this directory..")
    alignmentParser.add_argument(
        '--skipMulti', action='store_true', help="Skip images with more than one face.")
    alignmentParser.add_argument('--verbose', action='store_true')

    args = parser.parse_args()

    if args.mode == 'computeMean':
        computeMeanMain(args)
    else:
        sock.sendall('started\n'.encode())
        alignMain(args)
        sock.sendall('Bye\n'.encode())
