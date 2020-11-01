import pymongo
import pickle

client = pymongo.MongoClient("139.196.218.238", 27017)
recommender_db = client['recommender']
user_recs_ncf_col = recommender_db['UserRecsNCF2']


def insert(to_insert):
    x = user_recs_ncf_col.insert_many(to_insert)
    # x = user_recs_ncf_col.insert_one(to_insert[0])
    print(x)


fileHandle = open('recommends.txt', 'rb')
to_insert = pickle.load(fileHandle)
fileHandle.close()
insert(to_insert)
