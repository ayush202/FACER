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
import random
import shutil

import openface
import openface.helper
from openface.data import iterImgs

fileDir = os.path.dirname(os.path.realpath(__file__))
modelDir = os.path.join(fileDir, 'openface-master', 'models')
dlibModelDir = os.path.join(modelDir, 'dlib')
openfaceModelDir = os.path.join(modelDir, 'openface')


def write(vals, fName):
    if os.path.isfile(fName):
        print("{} exists. Backing up.".format(fName))
        os.rename(fName, "{}.bak".format(fName))
    with open(fName, 'w') as f:
        for p in vals:
            f.write(",".join(str(x) for x in p))
            f.write("\n")


def alignMain(args):
    openface.helper.mkdirP(args['outputDir'])
    imgs = list(iterImgs(args['inputDir']))
    print('reached here '+str(len(imgs)))
    # Shuffle so multiple versions can be run at once.
    random.shuffle(imgs)

    landmarkMap = {
        'outerEyesAndNose': openface.AlignDlib.OUTER_EYES_AND_NOSE,
        'innerEyesAndBottomLip': openface.AlignDlib.INNER_EYES_AND_BOTTOM_LIP
    }
    if args['landmarks'] not in landmarkMap:
        raise Exception("Landmarks unrecognized: {}".format(args.landmarks))

    landmarkIndices = landmarkMap[args['landmarks']]

    align = openface.AlignDlib(args['dlibFacePredictor'])

    nFallbacks = 0
    for imgObject in imgs:
        print("=== {} ===".format(imgObject.path))
        outDir = os.path.join(args['outputDir'], imgObject.cls)
        openface.helper.mkdirP(outDir)
        outputPrefix = os.path.join(outDir, imgObject.name)
        imgName = outputPrefix + ".png"
        if os.path.isfile(imgName):
            if args['verbose']:
                print("  + Already found, skipping.")
        else:
            rgb = imgObject.getRGB()
            if rgb is None:
                if args['verbose']:
                    print("  + Unable to load.")
                outRgb = None
            else:
                outRgb = align.align(args['size'], rgb,
                                     landmarkIndices=landmarkIndices,
                                     skipMulti=args['skipMulti'])
                if outRgb is None and args['verbose']:
                    print("  + Unable to align.")

            #if args.fallbackLfw and outRgb is None:
                #nFallbacks += 1
                #deepFunneled = "{}/{}.jpg".format(os.path.join(args.fallbackLfw,
                #                                               imgObject.cls),
               #                                   imgObject.name)
              #  shutil.copy(deepFunneled, "{}/{}.jpg".format(os.path.join(args.outputDir,
                 #                                                         imgObject.cls),
                   #                                          imgObject.name))

            if outRgb is not None:
                if args['verbose']:
                    print("  + Writing aligned file to disk.")
                outBgr = cv2.cvtColor(outRgb, cv2.COLOR_RGB2BGR)
                cv2.imwrite(imgName, outBgr)

    #if args.fallbackLfw:
       # print('nFallbacks:', nFallbacks)

if __name__ == '__main__':
    #parser = argparse.ArgumentParser()
    x = {}
    #parser.add_argument('inputDir', type=str, help="Input image directory.")
    x['inputDir'] = './openface-master/training-images'
    #parser.add_argument('--dlibFacePredictor', type=str, help="Path to dlib's face predictor.",
                       # default=os.path.join(dlibModelDir, "shape_predictor_68_face_landmarks.dat"))
    x['dlibFacePredictor'] = os.path.join(dlibModelDir, "shape_predictor_68_face_landmarks.dat")
    #subparsers = parser.add_subparsers(dest='mode', help="Mode")
    #computeMeanParser = subparsers.add_parser(
     #   'computeMean', help='Compute the image mean of a directory of images.')
    #computeMeanParser.add_argument('--numImages', type=int, help="The number of images. '0' for all images.",
     #                              default=0)  # <= 0 ===> all imgs
    #alignmentParser = subparsers.add_parser(
        #'align', help='Align a directory of images.')
    x['landmarks'] = 'outerEyesAndNose'
    #alignmentParser.add_argument('landmarks', type=str,
         #                        choices=['outerEyesAndNose',
              #                            'innerEyesAndBottomLip',
              #                            'eyes_1'],
                     #            help='The landmarks to align to.')
    x['outputDir'] = './openface-master/aligned-images/'
    #alignmentParser.add_argument(
       # 'outputDir', type=str, help="Output directory of aligned images.")
    x['size'] = 100
    #alignmentParser.add_argument('--size', type=int, help="Default image size.",
     #                            default=96)
    #alignmentParser.add_argument('--fallbackLfw', type=str,
     #                            help="If alignment doesn't work, fallback to copying the deep funneled version from this directory..")
    #alignmentParser.add_argument(
     #   '--skipMulti', action='store_true', help="Skip images with more than one face.")
    x['skipMulti'] = 'store_true'
    #alignmentParser.add_argument('--verbose', action='store_true')
    x['verbose'] = 'store_true'
    #args = parser.parse_args()

    #if args.mode == 'computeMean':
     #   computeMeanMain(args)
    #else:
    alignMain(x)