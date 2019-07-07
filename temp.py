# - *- coding: utf- 8 - *-
'''from PIL import Image
img = Image.open('4.jpg')
height,width = img.size
print(str(width)+"\n"+str(height))

import numpy as np
import dlib
import pandas as pd
x =[[128.01460296,96.81608364, 210.08275274 ,196.80317011 , 0.99632013]]
y = np.array(x)
la = pd.DataFrame(np.ndarray.tolist(y))
la.to_csv("hit_try.csv", index=False, header=False)
print(y)
print(np.shape(y))
#for box in y:
 #   print(box)
  #  print(dlib.rectangle(int(round(box[0])),int(round(box[1])),int(round(box[2])),int(round(box[3]))))
'''
import os

import pandas as pd
import numpy as np
'''
def double_check(num,rep):
    temp = pd.read_csv("/home/ayush/NetBeansProjects/face_recognition"
                            "/map_indexing.facer",header=None)
    temp = np.asarray(temp.as_matrix(),dtype=int)
    map_index = dict(zip(temp[:,0],zip(temp[:,1],temp[:,2])))
    print(map_index(1))
'''
from torch.autograd import Variable
import cv2
from PIL import Image,ExifTags
import PIL
import time
import os
'''def autoresize(path):
    st = time.time()
    #image = cv2.imread(path)
    img1 = Image.open(path)
    for orientation in ExifTags.TAGS.keys():
        if ExifTags.TAGS[orientation] == 'Orientation': break
    exif = dict(img1._getexif().items())
    print "orientation "+str(exif[orientation])
    if exif[orientation] == 3:
        img1 = img1.rotate(180, expand=True)
    elif exif[orientation] == 6:
        img1 = img1.rotate(270, expand=True)
    elif exif[orientation] == 8:
        img1 = img1.rotate(90, expand=True)
    #print type(image)
    print type(img1)
    print str(img1.size[0])+" "+str(img1.size[1])
    re = img1.resize((int(img1.size[0]/4), int(img1.size[1]/4)), Image.ANTIALIAS)
    re.save("/home/ayush/PycharmProjects/face_reco/123.jpg", quality=100)
    print str(re.size[0]) + " " + str(re.size[1])
    #os.remove("/home/ayush/PycharmProjects/face_reco/123.jpg")
    print time.time() - st
    return True
'''
'''img = cv2.cvtColor(img,cv2.COLOR_RGB2GRAY)
cv2.imwrite("/home/ayush/Downloads/temp1.jpg",img)'''
#autoresize("/home/ayush/PycharmProjects/face_reco/openface-master/training-images/aditya/IMG_20180301_124913.jpg")
import cv2
import sys

imagePath = '/home/ayush/Desktop/images/IMG-20151231-WA0052.jpg'

image = cv2.imread(imagePath)
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

faceCascade = cv2.CascadeClassifier(cv2.data.haarcascades + "haarcascade_frontalface_default.xml")
faces = faceCascade.detectMultiScale(
    gray,
    scaleFactor=1.3,
    minNeighbors=3,
    minSize=(30, 30)
)

print("[INFO] Found {0} Faces.".format(len(faces)))

for (x, y, w, h) in faces:
    cv2.rectangle(image, (x, y), (x + w, y + h), (0, 255, 0), 2)
    roi_color = image[y:y + h, x:x + w]
    print("[INFO] Object found. Saving locally.")
    cv2.imwrite(str(w) + str(h) + '_faces.jpg', roi_color)

status = cv2.imwrite('faces_detected.jpg', image)
print("[INFO] Image faces_detected.jpg written to filesystem: ", status)

'''
x = pd.read_csv("/home/ayush/PycharmProjects/face_reco/openface-master/tmp.csv"
                ,header=None)
y = np.asarray(x.as_matrix(),dtype=float).ravel()


x1 = pd.read_csv("/home/ayush/PycharmProjects/face_reco/openface-master/generated-embeddings/reps.csv")

y1 = np.asarray(x1.as_matrix(),dtype=float)

y1 = y1[406:441,:]
#print(y1[31:])

l = []
count =0
for u in y1:
    d = u-y
    z = np.dot(d,d)
    l.append(z)
    if z <=0.30:
        count+=1
    print(z)
ans = np.sum(l)
print("sum "+str(ans))
print("mean "+str(ans/len(l)))
print("count "+str(count))

l = y1[0]
z = len(y1)
for i in range(1,len(y1)):
    l = np.add(l,y1[i])

print("z ")
print(z)
l /= z
d = l-y
d = np.dot(d,d)'''
#print("sum "+str(d))