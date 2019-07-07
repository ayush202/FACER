import torch
from torch.autograd import Variable
from pytorch_repo import net
import os
import cv2
import numpy as np
import pandas as pd
from openface.data import iterImgs
import socket

f = open('PATH_to_Python',mode='r')
path = f.read().splitlines()[0]

model = net.model
model.load_state_dict(torch.load(path+'/pytorch_repo/models/nn4.small2.v1.pth'))
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

HOST = "localhost"
PORT = 8080
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((HOST, PORT))



def generate(inputDir):
    imgs = []
    for i in range(0,len(inputDir)):
        img = list(iterImgs(inputDir[i]))
        imgs += img
    sock.sendall('length\n'.encode())
    sock.sendall((str(len(imgs))+'\n').encode())
    print(len(imgs))
    i=0
    j=0
    label = []
    rep = []
    for imgObject in imgs:
        #print("=== {} ===".format(imgObject.path))
        img = cv2.imread(imgObject.path)
        if img is None:
            sock.sendall('unable to load\n'.encode())
            continue
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
        img = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)
        img = cv2.resize(img, (96, 96), interpolation=cv2.INTER_LINEAR)
        img = np.transpose(img, (2, 0, 1))
        img = img.astype(np.float32) / 255.0
        input = torch.from_numpy(img).unsqueeze(0)
        output = model(Variable(input))
        output = output.data.numpy()
        rep.append(output[0,:])
        sock.sendall('one complete\n'.encode())
        label.append(imgObject.path)
        j += 1
        if j % 50 == 0:
            print("represented "+str(j)+"/"+str(len(imgs)))
    label = np.array(label)
    images = []
    for i in range(0,len(rep)):
        temp = [label[i]]
        for j in range(i+1,len(rep)):
            d = rep[j] - rep[i]
            d = np.dot(d,d)
            if d <= 0.02:
                temp.append(label[j])
        if len(temp) > 1:
            images.append(temp)
        sock.sendall('one complete\n'.encode())
    '''d = rep[1]-rep[0]
    print(np.shape(rep[1]))
    print(np.dot(d,d))
    rep = np.array(rep)
    print(np.shape(rep))
    la = pd.DataFrame(label)
    la.to_csv(args.outputDir+"/labels1.csv", mode='w', index=False, header=False)
    re = pd.DataFrame(rep)
    re.to_csv(args.outputDir+"/reps1.csv", mode='w', index=False, header=False)
    '''
    images = pd.DataFrame(images)
    images.to_csv("tmp.csv", mode='w', index=False, header=False)

if __name__ == '__main__':
    #os.chdir('..')
    #os.chdir('openface-master')
    #print(os.getcwd())

    sock.sendall('started\n'.encode())
    val = sock.recv(1 << 16).decode()
    inputDir = []
    inputDir += list(val.split(","))
    print(inputDir)
    generate(inputDir)
    sock.sendall('Bye\n'.encode())
