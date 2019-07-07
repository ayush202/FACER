import torch
from torch.autograd import Variable
from pytorch_repo import net
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

'''
#input = torch.rand(6, 3, 96, 96)    # input: RGB image of 96*96
input = cv2.imread('/home/ayush/PycharmProjects/face_reco/10.png')
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
    sock.sendall((str(len(imgs))+'\n').encode())

    j=0
    label = []
    rep = []
    for imgObject in imgs:
        #print("=== {} ===".format(imgObject.path))
        #y = imgObject.path.split('/')
        #print(imgObject.path)
        #print(y)
        img = cv2.imread(imgObject.path)
        img = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)
        img = cv2.resize(img, (96, 96), interpolation=cv2.INTER_LINEAR)
        img = np.transpose(img, (2, 0, 1))
        img = img.astype(np.float32) / 255.0
        input = torch.from_numpy(img).unsqueeze(0)
        output = model(Variable(input))
        output = output.data.numpy()
        rep.append(output[0,:])
        sock.sendall('added\n'.encode())
        g = [args.name, imgObject.path]
        label.append(g)
        j += 1
        if j%50==0:
            print("represented "+str(j)+"/"+str(len(imgs)))
            sock.sendall(("represented "+str(j)+"/"+str(len(imgs))).encode())
    label = np.array(label)
    rep = np.array(rep)
    #print(np.shape(rep))
    la = pd.DataFrame(label)
    outfile = open(args.outputDir+"/labels.csv", mode='a')
    la.to_csv(outfile, index=False, header=False)
    outfile.close()
    re = pd.DataFrame(rep)
    outfile = open(args.outputDir+"/reps.csv", mode='a')
    re.to_csv(outfile, index=False, header=False)
    outfile.close()


if __name__ == '__main__':
    #os.chdir('..')
    #os.chdir('/home/ayush/PycharmProjects/face_reco/openface-master')
    print(os.getcwd())
    #sys.argv = "xyz ./aligned-images/5 ./generated-embeddings --name 5".split()
    parser = argparse.ArgumentParser()
    parser.add_argument('inputDir', type=str, help="Input image directory.")
    parser.add_argument('outputDir', type=str, help="Output directory of aligned images.")
    parser.add_argument('--name', type=str, help='name of the person')

    args = parser.parse_args()

    sock.sendall('started\n'.encode())
    generate(args)
    sock.sendall('Bye\n'.encode())