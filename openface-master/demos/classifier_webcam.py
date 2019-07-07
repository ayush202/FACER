#!/usr/bin/env python3
import time
#
# Example to run classifier on webcam stream.
# Brandon Amos & Vijayenthiran
# 2016/06/21
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

# Contrib: Vijayenthiran
# This example file shows to run a classifier on webcam stream. You need to
# run the classifier.py to generate classifier with your own dataset.
# To run this file from the openface home dir:
# ./demo/classifier_webcam.py <path-to-your-classifier>



start = time.time()

import argparse
import cv2
import os
import pickle
import sys
from pytorch_repo import net
import torch
from torch.autograd import Variable

import pandas as pd

import numpy as np
np.set_printoptions(precision=2)
from sklearn.mixture import GMM
import openface

fileDir = os.path.dirname(os.path.realpath(__file__))
modelDir = os.path.join(fileDir, '..', 'models')
dlibModelDir = os.path.join(modelDir, 'dlib')
#openfaceModelDir = os.path.join(modelDir, 'openface')

f = open('PATH_to_Python',mode='r')
path = f.read().splitlines()[0]

model = net.model
model.load_state_dict(torch.load(path+'/pytorch_repo/models/nn4.small2.v1.pth'))
model.eval()

x1 = pd.read_csv(path+'/openface-master/generated-embeddings/reps.csv', header=None)
y1 = np.asarray(x1.values,dtype=float)
print(os.getcwd())
temp = pd.read_csv('map_indexing.facer', header=None)
temp = np.asarray(temp.values, dtype=int)
map_index = dict(zip(temp[:, 0], zip(temp[:, 1], temp[:, 2])))
def double_check(num,rep):
    rep = rep.ravel()
    start = map_index[num][0]-1
    end = map_index[num][1]
    #print("start "+str(start)+" end "+str(end))
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
        #print("count "+str(count))
        #print(l)
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
        return False
    else:
        return True


def getRep(bgrImg):
    start = time.time()
    if bgrImg is None:
        raise Exception("Unable to load image/frame")
    '''width = bgrImg.shape[1]
    height = bgrImg.shape[0]
    check = False
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

    start = time.time()

    # Get the largest face bounding box
    # bb = align.getLargestFaceBoundingBox(rgbImg) #Bounding box

    # Get all bounding boxes
    bb = align.getAllFaceBoundingBoxes(rgbImg)
    print('bb')
    print(type(bb))
    print(bb)

    if bb is None:
        # raise Exception("Unable to find a face: {}".format(imgPath))
        return None
    if args.verbose:
        print("Face detection took {} seconds.".format(time.time() - start))

    start = time.time()

    alignedFaces = []
    for box in bb:
        alignedFaces.append(
            align.align(
                args.imgDim,
                rgbImg,
                box,
                landmarkIndices=openface.AlignDlib.OUTER_EYES_AND_NOSE))

    if alignedFaces is None:
        raise Exception("Unable to align the frame")
    if args.verbose:
        print("Alignment took {} seconds.".format(time.time() - start))

    start = time.time()

    reps = []
    scaled = []
    scaled_reshape = []
    for alignedFace in alignedFaces:
        print('alignedFace')
        print(type(alignedFace))
        print(np.shape(alignedFace))
        img = cv2.cvtColor(alignedFace, cv2.COLOR_RGB2BGR)
        img = cv2.resize(img, (96, 96), interpolation=cv2.INTER_LINEAR)
        img = np.transpose(img, (2, 0, 1))
        img = img.astype(np.float32) / 255.0
        input = torch.from_numpy(img).unsqueeze(0)
        output = model(Variable(input))
        output = output.data.numpy()
        #reps.append(net.forward(alignedFace))
        reps.append(output[0,:])

    if args.verbose:
        print("Neural network forward pass took {} seconds.".format(
            time.time() - start))

    # print (reps)
    return (reps,bb)


def infer(img, args):
    try:
        with open(args.classifierModel, 'rb') as f:
            if sys.version_info[0] < 3:
                (le, clf) = pickle.load(f)  # le - label and clf - classifer
            else:
                (le, clf) = pickle.load(f, encoding='bytes')  # le - label and clf - classifer
    except Exception as e:
        sys.stderr.write('classifier.pkl cannot be opened')
        raise Exception('classifier.pkl cannot be opened')
    repsAndBBs = getRep(img)
    reps = repsAndBBs[0]
    bbs = repsAndBBs[1]
    persons = []
    confidences = []
    y = []
    for rep in reps:
        try:
            rep = rep.reshape(1, -1)
            rep1 = np.array(rep)
        except:
            print ("No Face detected")
            return (None, None)
        start = time.time()
        predictions = clf.predict_proba(rep).ravel()
        # print (predictions)
        maxI = np.argmax(predictions)
        # max2 = np.argsort(predictions)[-3:][::-1][1]
        x = le.inverse_transform(maxI)
        if x != '1' and predictions[maxI] >= 0.50:
            val = double_check(int(x), rep1)
            if not val:
                predictions[maxI] = predictions[maxI]
            y = rep1
        persons.append(le.inverse_transform(maxI))
        # print (str(le.inverse_transform(max2)) + ": "+str( predictions [max2]))
        # ^ prints the second prediction
        confidences.append(predictions[maxI])
        if args.verbose:
            print("Prediction took {} seconds.".format(time.time() - start))
            pass
        # print("Predict {} with {:.2f} confidence.".format(person.decode('utf-8'), confidence))
        if isinstance(clf, GMM):
            dist = np.linalg.norm(rep - clf.means_[maxI])
            print("  + Distance from the mean: {}".format(dist))
            pass
    la = pd.DataFrame(y)
    la.to_csv('tmp.csv', index=False, header=False)
    return (persons, confidences ,bbs)


if __name__ == '__main__':

    #os.chdir('..')
    #sys.argv = "xyz  --captureDevice 0  --width 500 --height 500 --verbose /home/ayush/PycharmProjects/face_reco/openface-master/generated-embeddings/classifier.pkl".split()

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
    parser.add_argument(
        '--captureDevice',
        type=int,
        default=0,
        help='Capture device. 0 for laptop webcam and 1 for usb webcam')
    parser.add_argument('--width', type=int, default=320)
    parser.add_argument('--height', type=int, default=240)
    parser.add_argument('--threshold', type=float, default=0.62)
    parser.add_argument('--cuda', action='store_true')
    parser.add_argument('--verbose', action='store_true')
    parser.add_argument(
        'classifierModel',
        type=str,
        help='The Python pickle representing the classifier. This is NOT the Torch network model, which can be set with --networkModel.')

    args = parser.parse_args()

    align = openface.AlignDlib(args.dlibFacePredictor)
    '''net = openface.TorchNeuralNet(
        args.networkModel,
        imgDim=args.imgDim,
        cuda=args.cuda)'''

    # Capture device. Usually 0 will be webcam and 1 will be usb cam.
    video_capture = cv2.VideoCapture(args.captureDevice)
    video_capture.set(3, args.width)
    video_capture.set(4, args.height)

    confidenceList = []
    outfile = open('map.facer', mode='r')
    d = {}
    li = outfile.read().split()
    d = dict(zip(li[::2], li[1::2]))
    d.update({'_unknown':'_unknown'})
    outfile.close()
    frame1 = ''
    #frame1 = cv2.resize(frame1, (700, 700))
    while True:
        ret, frame = video_capture.read()
        #frame = cv2.imread('/home/ayush/Desktop/images/IMG-20151231-WA0052.jpg')
        #frame = cv2.resize(frame, (1000, 500))

        print('frame1')
        print(np.shape(frame))
        persons, confidences, bbs = infer(frame, args)
        print ("P: " + str(persons) + " C: " + str(confidences))
        try:
            # append with two floating point precision
            confidenceList.append('%.2f' % confidences[0])
        except:
            # If there is no face detected, confidences matrix will be empty.
            # We can simply ignore it.
            pass

        for i, c in enumerate(confidences):
            if c <= args.threshold:  # 0.5 is kept as threshold for known face.
                persons[i] = "_unknown"

        # Print the person name and conf value on the frame next to the person
        # Also print the bounding box
        img = []
        for idx,person in enumerate(persons):
            cv2.rectangle(frame, (bbs[idx].left(), bbs[idx].top()), (bbs[idx].right(), bbs[idx].bottom()), (0, 255, 0), 2)
            cv2.putText(frame, "{} @{:.2f}".format(d[person], confidences[idx]),
                        (bbs[idx].left(), bbs[idx].bottom()+20), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 0), 1)
            cv2.putText(frame, "Press q to Exit.",
                        (70, 20), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (115, 50, 240), 1)
        cv2.imshow('',frame)
        # quit the program on the press of key 'q'
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break
    # When everything is done, release the capture
    video_capture.release()
    cv2.destroyAllWindows()
