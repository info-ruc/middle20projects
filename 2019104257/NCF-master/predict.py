import os
import time
import argparse
import numpy as np

import torch
import torch.nn as nn
import torch.optim as optim
import torch.utils.data as data
import torch.backends.cudnn as cudnn
# from tensorboardX import SummaryWriter

import model
import config
import evaluate
import data_utils
import mongodb_util
import pickle

parser = argparse.ArgumentParser()
parser.add_argument("--lr",
                    type=float,
                    default=0.001,
                    help="learning rate")
parser.add_argument("--dropout",
                    type=float,
                    default=0.0,
                    help="dropout rate")
parser.add_argument("--batch_size",
                    type=int,
                    default=256,
                    help="batch size for training")
parser.add_argument("--epochs",
                    type=int,
                    default=20,
                    help="training epoches")
parser.add_argument("--top_k",
                    type=int,
                    default=10,
                    help="compute metrics@top_k")
parser.add_argument("--factor_num",
                    type=int,
                    default=32,
                    help="predictive factors numbers in the model")
parser.add_argument("--num_layers",
                    type=int,
                    default=3,
                    help="number of layers in MLP model")
parser.add_argument("--num_ng",
                    type=int,
                    default=4,
                    help="sample negative items for training")
parser.add_argument("--test_num_ng",
                    type=int,
                    default=99,
                    help="sample part of negative items for testing")
parser.add_argument("--out",
                    default=True,
                    help="save model or not")
parser.add_argument("--gpu",
                    type=str,
                    default="0",
                    help="gpu card ID")
args = parser.parse_args()

# os.environ["CUDA_VISIBLE_DEVICES"] = args.gpu
cudnn.benchmark = True

############################## PREPARE DATASET ##########################
train_data, test_data, user_num, item_num, train_mat = data_utils.load_all()
# test_data = data_utils.load_all_for_predict()

# construct the train and test datasets
train_dataset = data_utils.NCFData(
    train_data, item_num, train_mat, args.num_ng, True)
test_dataset = data_utils.NCFData(
    test_data, item_num, train_mat, 0, False)
train_loader = data.DataLoader(train_dataset,
                               batch_size=args.batch_size, shuffle=True, num_workers=4)
test_loader = data.DataLoader(test_dataset,
                              batch_size=args.test_num_ng + 1, shuffle=False, num_workers=0)

########################### CREATE MODEL #################################
if config.model == 'NeuMF-pre':
    assert os.path.exists(config.GMF_model_path), 'lack of GMF model'
    assert os.path.exists(config.MLP_model_path), 'lack of MLP model'
    GMF_model = torch.load(config.GMF_model_path)
    MLP_model = torch.load(config.MLP_model_path)
else:
    GMF_model = None
    MLP_model = None

# model = model.NCF(user_num, item_num, args.factor_num, args.num_layers,
# 						args.dropout, config.model, GMF_model, MLP_model)
model = torch.load('{}{}.pth'.format(config.model_path, config.model), map_location='cpu')
print("model", model)
# model.load_state_dict(dict)

# model.cuda()


# writer = SummaryWriter() # for visualization


model.eval()
# HR, NDCG = evaluate.metrics(model, test_loader, args.top_k)

to_insert = []
for user, item, label in test_loader:
    # print('user', user)
    # print('item', item)
    # user = user.cuda()
    # item = item.cuda()

    predictions = model(user, item)
    # print("prediction.shape", predictions.shape)
    _, indices = torch.topk(predictions, 10)
    recommends = torch.take(
        item, indices).numpy().tolist()
    to_insert.append({"uid": user.numpy().tolist()[0], "recs": recommends})
# print("re",recommends)
print(to_insert)
# mongodb_util.insert(to_insert)
fileHandle = open('recommends.txt', 'wb')

pickle.dump(to_insert, fileHandle)
fileHandle.close()
