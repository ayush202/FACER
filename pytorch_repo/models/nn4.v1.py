
import torch
import torch.nn as nn
from torch.autograd import Variable

from layer.lambdas import LambdaBase, Lambda, LambdaMap, LambdaReduce
from layer.normalize import Normalize
from layer.spatiallppooling import SpatialLPPooling
from layer.depthconcat import concat_with_pad
from layer.spatialcrossmaplrn import SpatialCrossMapLRN


/root/data/Converter/OpenFace/model =    nn.Sequential( # Sequential,
       nn.Conv2d(3, 64, (7, 7), (2, 2), (3, 3), 1, 1,bias=True),
       nn.BatchNorm2d(64),
       nn.ReLU(),
       nn.MaxPool2d((3, 3), (2, 2), (1, 1)),
       nn.Conv2d(64, 64, (1, 1), (1, 1), (0, 0), 1, 1,bias=True),
       nn.BatchNorm2d(64),
       nn.ReLU(),
       nn.Conv2d(64, 192, (3, 3), (1, 1), (1, 1), 1, 1,bias=True),
       nn.BatchNorm2d(192),
       nn.ReLU(),
       nn.MaxPool2d((3, 3), (2, 2), (1, 1)),
       nn.Sequential( # Sequential,
           LambdaReduce(lambda x, y, dim=1: concat_with_pad((x, y), dim),    # DepthConcat,
               nn.Sequential( # Sequential,
                   nn.Conv2d(192, 96, (1, 1), (1, 1)),
                   nn.BatchNorm2d(96),
                   nn.ReLU(),
                   nn.Conv2d(96, 128, (3, 3), (1, 1), (1, 1)),
                   nn.BatchNorm2d(128),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(192, 16, (1, 1), (1, 1)),
                   nn.BatchNorm2d(16),
                   nn.ReLU(),
                   nn.Conv2d(16, 32, (5, 5), (1, 1), (2, 2)),
                   nn.BatchNorm2d(32),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.MaxPool2d((3, 3), (2, 2)),
                   nn.Conv2d(192, 32, (1, 1), (1, 1)),
                   nn.BatchNorm2d(32),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(192, 64, (1, 1), (1, 1)),
                   nn.BatchNorm2d(64),
                   nn.ReLU(),
               ),
           ),
       ),
       nn.Sequential( # Sequential,
           LambdaReduce(lambda x, y, dim=1: concat_with_pad((x, y), dim),    # DepthConcat,
               nn.Sequential( # Sequential,
                   nn.Conv2d(256, 96, (1, 1), (1, 1)),
                   nn.BatchNorm2d(96),
                   nn.ReLU(),
                   nn.Conv2d(96, 128, (3, 3), (1, 1), (1, 1)),
                   nn.BatchNorm2d(128),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(256, 32, (1, 1), (1, 1)),
                   nn.BatchNorm2d(32),
                   nn.ReLU(),
                   nn.Conv2d(32, 64, (5, 5), (1, 1), (2, 2)),
                   nn.BatchNorm2d(64),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   SpatialLPPooling(2, 3, 3, 3, 3),    #SpatialLPPooling,
                   nn.Conv2d(256, 64, (1, 1), (1, 1)),
                   nn.BatchNorm2d(64),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(256, 64, (1, 1), (1, 1)),
                   nn.BatchNorm2d(64),
                   nn.ReLU(),
               ),
           ),
       ),
       nn.Sequential( # Sequential,
           LambdaReduce(lambda x, y, dim=1: concat_with_pad((x, y), dim),    # DepthConcat,
               nn.Sequential( # Sequential,
                   nn.Conv2d(320, 128, (1, 1), (1, 1)),
                   nn.BatchNorm2d(128),
                   nn.ReLU(),
                   nn.Conv2d(128, 256, (3, 3), (2, 2), (1, 1)),
                   nn.BatchNorm2d(256),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(320, 32, (1, 1), (1, 1)),
                   nn.BatchNorm2d(32),
                   nn.ReLU(),
                   nn.Conv2d(32, 64, (5, 5), (2, 2), (2, 2)),
                   nn.BatchNorm2d(64),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.MaxPool2d((3, 3), (2, 2)),
               ),
           ),
       ),
       nn.Sequential( # Sequential,
           LambdaReduce(lambda x, y, dim=1: concat_with_pad((x, y), dim),    # DepthConcat,
               nn.Sequential( # Sequential,
                   nn.Conv2d(640, 96, (1, 1), (1, 1)),
                   nn.BatchNorm2d(96),
                   nn.ReLU(),
                   nn.Conv2d(96, 192, (3, 3), (1, 1), (1, 1)),
                   nn.BatchNorm2d(192),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(640, 32, (1, 1), (1, 1)),
                   nn.BatchNorm2d(32),
                   nn.ReLU(),
                   nn.Conv2d(32, 64, (5, 5), (1, 1), (2, 2)),
                   nn.BatchNorm2d(64),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   SpatialLPPooling(2, 3, 3, 3, 3),    #SpatialLPPooling,
                   nn.Conv2d(640, 128, (1, 1), (1, 1)),
                   nn.BatchNorm2d(128),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(640, 256, (1, 1), (1, 1)),
                   nn.BatchNorm2d(256),
                   nn.ReLU(),
               ),
           ),
       ),
       nn.Sequential( # Sequential,
           LambdaReduce(lambda x, y, dim=1: concat_with_pad((x, y), dim),    # DepthConcat,
               nn.Sequential( # Sequential,
                   nn.Conv2d(640, 112, (1, 1), (1, 1)),
                   nn.BatchNorm2d(112),
                   nn.ReLU(),
                   nn.Conv2d(112, 224, (3, 3), (1, 1), (1, 1)),
                   nn.BatchNorm2d(224),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(640, 32, (1, 1), (1, 1)),
                   nn.BatchNorm2d(32),
                   nn.ReLU(),
                   nn.Conv2d(32, 64, (5, 5), (1, 1), (2, 2)),
                   nn.BatchNorm2d(64),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   SpatialLPPooling(2, 3, 3, 3, 3),    #SpatialLPPooling,
                   nn.Conv2d(640, 128, (1, 1), (1, 1)),
                   nn.BatchNorm2d(128),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(640, 224, (1, 1), (1, 1)),
                   nn.BatchNorm2d(224),
                   nn.ReLU(),
               ),
           ),
       ),
       nn.Sequential( # Sequential,
           LambdaReduce(lambda x, y, dim=1: concat_with_pad((x, y), dim),    # DepthConcat,
               nn.Sequential( # Sequential,
                   nn.Conv2d(640, 128, (1, 1), (1, 1)),
                   nn.BatchNorm2d(128),
                   nn.ReLU(),
                   nn.Conv2d(128, 256, (3, 3), (1, 1), (1, 1)),
                   nn.BatchNorm2d(256),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(640, 32, (1, 1), (1, 1)),
                   nn.BatchNorm2d(32),
                   nn.ReLU(),
                   nn.Conv2d(32, 64, (5, 5), (1, 1), (2, 2)),
                   nn.BatchNorm2d(64),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   SpatialLPPooling(2, 3, 3, 3, 3),    #SpatialLPPooling,
                   nn.Conv2d(640, 128, (1, 1), (1, 1)),
                   nn.BatchNorm2d(128),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(640, 192, (1, 1), (1, 1)),
                   nn.BatchNorm2d(192),
                   nn.ReLU(),
               ),
           ),
       ),
       nn.Sequential( # Sequential,
           LambdaReduce(lambda x, y, dim=1: concat_with_pad((x, y), dim),    # DepthConcat,
               nn.Sequential( # Sequential,
                   nn.Conv2d(640, 144, (1, 1), (1, 1)),
                   nn.BatchNorm2d(144),
                   nn.ReLU(),
                   nn.Conv2d(144, 288, (3, 3), (1, 1), (1, 1)),
                   nn.BatchNorm2d(288),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(640, 32, (1, 1), (1, 1)),
                   nn.BatchNorm2d(32),
                   nn.ReLU(),
                   nn.Conv2d(32, 64, (5, 5), (1, 1), (2, 2)),
                   nn.BatchNorm2d(64),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   SpatialLPPooling(2, 3, 3, 3, 3),    #SpatialLPPooling,
                   nn.Conv2d(640, 128, (1, 1), (1, 1)),
                   nn.BatchNorm2d(128),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(640, 160, (1, 1), (1, 1)),
                   nn.BatchNorm2d(160),
                   nn.ReLU(),
               ),
           ),
       ),
       nn.Sequential( # Sequential,
           LambdaReduce(lambda x, y, dim=1: concat_with_pad((x, y), dim),    # DepthConcat,
               nn.Sequential( # Sequential,
                   nn.Conv2d(640, 160, (1, 1), (1, 1)),
                   nn.BatchNorm2d(160),
                   nn.ReLU(),
                   nn.Conv2d(160, 256, (3, 3), (2, 2), (1, 1)),
                   nn.BatchNorm2d(256),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(640, 64, (1, 1), (1, 1)),
                   nn.BatchNorm2d(64),
                   nn.ReLU(),
                   nn.Conv2d(64, 128, (5, 5), (2, 2), (2, 2)),
                   nn.BatchNorm2d(128),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.MaxPool2d((3, 3), (2, 2)),
               ),
           ),
       ),
       nn.Sequential( # Sequential,
           LambdaReduce(lambda x, y, dim=1: concat_with_pad((x, y), dim),    # DepthConcat,
               nn.Sequential( # Sequential,
                   nn.Conv2d(1024, 192, (1, 1), (1, 1)),
                   nn.BatchNorm2d(192),
                   nn.ReLU(),
                   nn.Conv2d(192, 384, (3, 3), (1, 1), (1, 1)),
                   nn.BatchNorm2d(384),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(1024, 48, (1, 1), (1, 1)),
                   nn.BatchNorm2d(48),
                   nn.ReLU(),
                   nn.Conv2d(48, 128, (5, 5), (1, 1), (2, 2)),
                   nn.BatchNorm2d(128),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   SpatialLPPooling(2, 3, 3, 3, 3),    #SpatialLPPooling,
                   nn.Conv2d(1024, 128, (1, 1), (1, 1)),
                   nn.BatchNorm2d(128),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(1024, 384, (1, 1), (1, 1)),
                   nn.BatchNorm2d(384),
                   nn.ReLU(),
               ),
           ),
       ),
       nn.Sequential( # Sequential,
           LambdaReduce(lambda x, y, dim=1: concat_with_pad((x, y), dim),    # DepthConcat,
               nn.Sequential( # Sequential,
                   nn.Conv2d(1024, 192, (1, 1), (1, 1)),
                   nn.BatchNorm2d(192),
                   nn.ReLU(),
                   nn.Conv2d(192, 384, (3, 3), (1, 1), (1, 1)),
                   nn.BatchNorm2d(384),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(1024, 48, (1, 1), (1, 1)),
                   nn.BatchNorm2d(48),
                   nn.ReLU(),
                   nn.Conv2d(48, 128, (5, 5), (1, 1), (2, 2)),
                   nn.BatchNorm2d(128),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.MaxPool2d((3, 3), (2, 2)),
                   nn.Conv2d(1024, 128, (1, 1), (1, 1)),
                   nn.BatchNorm2d(128),
                   nn.ReLU(),
               ),
               nn.Sequential( # Sequential,
                   nn.Conv2d(1024, 384, (1, 1), (1, 1)),
                   nn.BatchNorm2d(384),
                   nn.ReLU(),
               ),
           ),
       ),
       nn.AvgPool2d((3, 3), (1, 1)),
       Lambda(lambda x: x.view(x.size(0), -1)), # View,
       nn.Sequential(Lambda(lambda x: x.view(1, -1) if 1==len(x.size()) else x ), nn.Linear(1024, 128)), # Linear,
       Normalize(2),    #Normalize,
   )