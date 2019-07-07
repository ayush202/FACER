#!/usr/bin/env python3
#
# Example to classify faces.
# Brandon Amos
# 2015/10/11
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

import time

start = time.time()

import argparse
import cv2
import os
import pickle
import sys
import dlib

from operator import itemgetter

import numpy as np
np.set_printoptions(precision=2)
import pandas as pd

import openface

from sklearn.pipeline import Pipeline
from sklearn.discriminant_analysis import LinearDiscriminantAnalysis as LDA
from sklearn.preprocessing import LabelEncoder
from sklearn.svm import SVC
from sklearn.model_selection import GridSearchCV
from sklearn.mixture import GMM
from sklearn.tree import DecisionTreeClassifier
from sklearn.naive_bayes import GaussianNB

from pytorch_repo import net
import torch
from torch.autograd import Variable
import socket

HOST = "localhost"
PORT = 8080
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((HOST, PORT))

f = open('PATH_to_Python',mode='r')
path = f.read().splitlines()[0]

model = net.model
model.load_state_dict(torch.load(path+'/pytorch_repo/models/nn4.small2.v1.pth'))
model.eval()


#print('reached here')
#print(os.getcwd())
fileDir = os.path.dirname(os.path.realpath(__file__))
#print('reached here1')
#print(fileDir)
modelDir = os.path.join(fileDir, '..', 'models')
dlibModelDir = os.path.join(modelDir, 'dlib')
#openfaceModelDir = os.path.join(modelDir, 'openface')



x1 = pd.read_csv(path+'/openface-master/generated-embeddings/reps.csv', header=None)
y1 = np.asarray(x1.values,dtype=float)
temp = pd.read_csv('map_indexing.facer', header=None)
temp = np.asarray(temp.values, dtype=int)
map_index = dict(zip(temp[:, 0], zip(temp[:, 1], temp[:, 2])))
def double_check(num,rep):
    rep = rep.ravel()
    start = map_index[num][0]-1
    end = map_index[num][1]
    tmp1 = y1[start:end,:]

    l = []
    count = 0
    for u in tmp1:
        diff = u - rep
        z = np.dot(diff, diff)
        l.append(z)
        if z <= 0.30:
            count += 1
        #print(z)
    '''ans = np.sum(l)
    print("sum " + str(ans))
    print("mean " + str(ans / len(l)))
    print("count " + str(count))'''
    if count <= 2:
        print('count '+str(count))
        if count == 0:
            print(l)
        return False

    l = tmp1[0]
    z = len(tmp1)
    for i in range(1, len(tmp1)):
        l = np.add(l, tmp1[i])

    print("z ")
    print(z)
    l /= z
    diff = l - rep
    diff = np.dot(diff, diff)
    #print("sum " + str(d))
    if diff > 0.52:
        print('diff '+str(diff))
        return False
    else:
        return True




def getRep(imgPath, multiple):
    start = time.time()
    bgrImg = cv2.imread(imgPath)
    if bgrImg is None:
        return False
        #raise Exception("Unable to load image: {}".format(imgPath))

    width = bgrImg.shape[1]
    height = bgrImg.shape[0]
    '''check = False
    if width > 600:
        width = 600
        check = True
    if height > 600:
        height = 600
        check = True
    if check:
        bgrImg = cv2.resize(bgrImg, (width, height), interpolation=cv2.INTER_LINEAR)
    '''
    rgbImg = cv2.cvtColor(bgrImg, cv2.COLOR_BGR2RGB)
    if args.verbose:
        print("  + Original size: {}".format(rgbImg.shape))
    if args.verbose:
        print("Loading the image took {} seconds.".format(time.time() - start))


    if multiple:
        bbs = align.getAllFaceBoundingBoxes(rgbImg)
    else:
        bb1 = align.getLargestFaceBoundingBox(rgbImg)
        bbs = [bb1]
    if len(bbs) == 0 or (not multiple and bb1 is None):
        print("Unable to find a face: {}".format(imgPath))
        return False
        sock.sendall(("Unable to find a face: {}\n".format(imgPath)).encode())
    if args.verbose:
        print("Face detection took {} seconds.".format(time.time() - start))

    reps = []
    for bb in bbs:
        start = time.time()
        alignedFace = align.align(
            args.imgDim,
            rgbImg,
            bb,
            landmarkIndices=openface.AlignDlib.OUTER_EYES_AND_NOSE)
        if alignedFace is None:
            print("Unable to align image: {}".format(imgPath))
            sock.sendall(("Unable to align image: {}\n".format(imgPath)).encode())
            continue
        if args.verbose:
            print("Alignment took {} seconds.".format(time.time() - start))
            print("This bbox is centered at {}, {}".format(bb.center().x, bb.center().y))

        start = time.time()
        print('aligned face')
        print(type(alignedFace))

        img = cv2.cvtColor(alignedFace, cv2.COLOR_RGB2BGR)
        img = cv2.resize(img, (96, 96), interpolation=cv2.INTER_LINEAR)
        img = np.transpose(img, (2, 0, 1))
        img = img.astype(np.float32) / 255.0
        input = torch.from_numpy(img).unsqueeze(0)
        output = model(Variable(input))
        output = output.data.numpy()
        rep = output[0,:]

        #rep = net.forward(alignedFace)
        if args.verbose:
            print("Neural network forward pass took {} seconds.".format(
                time.time() - start))
        reps.append((bb.center().x, rep))
    sreps = sorted(reps, key=lambda x: x[0])
    return sreps



def infer(args, multiple, d, le, clf):

    collect = []
    for img in args.imgs:
        print("\n=== {} ===".format(img))
        reps = getRep(img, multiple)
        if isinstance(reps, bool):
            sock.sendall("one completed\n".encode())
            continue
        if len(reps) > 1:
            print("List of faces in image from left to right")
        for r in reps:
            rep = r[1].reshape(1, -1)
            rep1 = np.array(rep)
            bbx = r[0]
            start = time.time()
            predictions = clf.predict_proba(rep).ravel()
            maxI = np.argmax(predictions)
            person = le.inverse_transform(maxI)
            confidence = predictions[maxI]
            if args.verbose:
                print("Prediction took {} seconds.".format(time.time() - start))
                sock.sendall(("Prediction took {} seconds.".format(time.time() - start)).encode())
            if multiple:
                tmp = person
                print("Predict  {} @ x={} with {:.2f} confidence.".format(d[tmp], bbx,
                                                                         confidence))
                if int(tmp) == args.find:
                    y = [img, confidence]
                    val = double_check(int(tmp),rep1)
                    if not val and confidence <= 0.95:
                        continue
                    collect.append(y)
                    break
                sock.sendall(("Predict {} @ x={} with {:.2f} confidence.\n".format(person, bbx,
                                                                         confidence)).encode())
            else:
                print("Predict  {} with {:.2f} confidence.".format(d[person], confidence))
                sock.sendall(("Predict {} with {:.2f} confidence.\n".format(person, confidence)).encode())
                collect.append(y)
            if isinstance(clf, GMM):
                dist = np.linalg.norm(rep - clf.means_[maxI])
                print("  + Distance from the mean: {}".format(dist))
        sock.sendall("one completed\n".encode())
    la = pd.DataFrame(collect)
    la.to_csv('tmp.csv', index=False, header=False, mode='a')

if __name__ == '__main__':
    #os.chdir('..')
    #sys.argv = "xyz --find 3 --path /home/ayush/Desktop/wallpapers                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     infer ./generated-embeddings/classifier.pkl xyz --multi".split()
    #print(sys.argv)
    parser = argparse.ArgumentParser()

    parser.add_argument(
        '--dlibFacePredictor',
        type=str,
        help="Path to dlib's face predictor.",
        default=os.path.join(
            dlibModelDir,
            "shape_predictor_68_face_landmarks.dat"))
    '''parser.add_argument(
        '--networkModel',
        type=str,
        help="Path to Torch network model.",
        default=os.path.join(
            openfaceModelDir,
            'nn4.small2.v1.t7'))'''
    parser.add_argument('--imgDim', type=int,
                        help="Default image dimension.", default=96)
    parser.add_argument('--cuda', action='store_true')
    parser.add_argument('--verbose', action='store_true')

    parser.add_argument('--path',type=str,help = "image directory")

    parser.add_argument('--find',type=int,help="id of the person")

    subparsers = parser.add_subparsers(dest='mode', help="Mode")

    inferParser = subparsers.add_parser(
        'infer', help='Predict who an image contains from a trained classifier.')
    inferParser.add_argument(
        'classifierModel',
        type=str,
        help='The Python pickle representing the classifier. This is NOT the Torch network model, which can be set with --networkModel.')
    inferParser.add_argument('imgs', type=str, nargs='+',
                             help="Input image.",default = "")
    inferParser.add_argument('--multi', help="Infer multiple faces in image",
                             action="store_true")

    args = parser.parse_args()
    if args.verbose:
        print("Argument parsing and import libraries took {} seconds.".format(
            time.time() - start))
        sock.sendall(("Argument parsing and import libraries took {} seconds.\n".format(
            time.time() - start)).encode())
    if args.mode == 'infer' and args.classifierModel.endswith(".t7"):
        raise Exception("""
Torch network model passed as the classification model,
which should be a Python pickle (.pkl)

See the documentation for the distinction between the Torch
network and classification models:

        http://cmusatyalab.github.io/openface/demo-3-classifier/
        http://cmusatyalab.github.io/openface/training-new-models/

Use `--networkModel` to set a non-standard Torch network model.""")
    start = time.time()

    align = openface.AlignDlib(args.dlibFacePredictor)
    '''net = openface.TorchNeuralNet(args.networkModel, imgDim=args.imgDim,
                                  cuda=args.cuda)'''

    if args.verbose:
        print("Loading the dlib and OpenFace models took {} seconds.".format(
            time.time() - start))
        sock.sendall(("Loading the dlib and OpenFace models took {} seconds.\n".format(
          time.time() - start)).encode())
        start = time.time()

    exts = [".jpg", ".jpeg", ".png", ".JPG"]
    list = []
    for subdir, dirs, files in os.walk(args.path):
        for path in files:
            (imageClass, fName) = (os.path.basename(subdir), path)
            (imageName, ext) = os.path.splitext(fName)
            if ext.lower() in exts:
                list.append(os.path.join(subdir, fName))

    sock.sendall("length\n".encode())
    sock.sendall((str(len(list))+"\n").encode())
    if args.mode == 'infer':
        sock.sendall("started\n".encode())
        args.imgs = []
        la = pd.DataFrame(args.imgs)
        la.to_csv('tmp.csv', index=False, header=False, mode='w')
        with open(args.classifierModel, 'rb') as f:
            if sys.version_info[0] < 3:
                (le, clf) = pickle.load(f)
            else:
                (le, clf) = pickle.load(f, encoding='bytes')
            outfile = open('map.facer', mode='r')

            li = outfile.read().split()
            d = dict(zip(li[::2], li[1::2]))
            d.update({'_unknown': '_unknown'})
            outfile.close()
        for i in range(0,len(list),10):
            args.imgs = []
            for j in range(i, min(i+10, len(list)), 1):
                args.imgs.append(list[j])
            infer(args, args.multi,d,le,clf)
        sock.sendall("Bye\n".encode())
