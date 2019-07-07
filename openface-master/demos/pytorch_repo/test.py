import torch
from torch.autograd import Variable
import net
import cv2
import numpy as np
import pandas as pd
import sys
import os
import argparse
import openface
import openface.helper
from openface.data import iterImgs
import random
import csv



model = net.model
model.load_state_dict(torch.load('./models/nn4.small2.v1.pth'))
model.eval()

'''
#input = torch.rand(6, 3, 96, 96)    # input: RGB image of 96*96
input = cv2.imread('/home/ayush/PycharmProjects/face_recognition/10.png')
input = np.asarray(input,dtype=np.float32)
input = input.transpose(2, 0, 1)
input = input.reshape(1, 3, 96, 96)
print(np.shape(input))
input = torch.from_numpy(input)


# CPU
output = model(Variable(input))
print('CPU done')
output = output.data.numpy()
la = pd.DataFrame(output)
la.to_csv("hit_try1.csv", mode='a', index=False, header=False)
'''

# GPU
'''
model.cuda()
input = input.cuda()
output = model(Variable(input))
print('GPU done')
'''

def generate(args):
    openface.helper.mkdirP(args.outputDir)
    imgs = list(iterImgs(args.inputDir))

    s = ''
    i=0
    j=0
    label = []
    rep = []
    for imgObject in imgs:
        #print("=== {} ===".format(imgObject.path))
        y = imgObject.path.split('/')
        img = cv2.imread(imgObject.path)
        img = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)
        img = cv2.resize(img, (96, 96), interpolation=cv2.INTER_LINEAR)
        img = np.transpose(img, (2, 0, 1))
        img = img.astype(np.float32) / 255.0
        input = torch.from_numpy(img).unsqueeze(0)
        output = model(Variable(input))
        output = output.data.numpy()
        rep.append(output[0,:])
        if s != y[2]:
            i += 1
            s = y[2]
        g = [i, imgObject.path]
        label.append(g)
        j+=1
        if j%50==0:
            print("represented "+str(j)+"/"+str(len(imgs)))
    label = np.array(label)
    d = rep[1]-rep[0]
    print(np.dot(d,d))
    rep = np.array(rep)
    print(np.shape(rep))
    la = pd.DataFrame(label)
    la.to_csv(args.outputDir+"/labels1.csv", mode='w', index=False, header=False)
    re = pd.DataFrame(rep)
    re.to_csv(args.outputDir+"/reps1.csv", mode='w', index=False, header=False)


if __name__ == '__main__':
    os.chdir('..')
    #os.chdir('openface-master')
    print(os.getcwd())
    sys.argv = "xyz /home/ayush/Desktop/images/birthday/temp ./".split()
    parser = argparse.ArgumentParser()
    parser.add_argument('inputDir', type=str, help="Input image directory.")
    parser.add_argument('outputDir', type=str, help="Output directory of aligned images.")

    args = parser.parse_args()
    generate(args)