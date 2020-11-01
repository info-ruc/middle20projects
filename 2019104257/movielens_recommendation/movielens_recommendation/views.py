import time
from enum import IntEnum

from django.http import HttpResponse
from django.http import JsonResponse

import pymongo
client = pymongo.MongoClient("139.196.218.238", 27017)
recommender_db = client['recommender']
user_recs_col=recommender_db['UserRecs']

def hello(request):
    return HttpResponse("Hello world ! ")


class resp_code(IntEnum):
    SUCCESS = 0
    INVALID_ARGUMENT = 1 #参数不齐全，或者值不符合要求
    DB_CONN_ERROR=2
    ERROR_UNKOWN = 17

def get_rec(request):

    # if request.method != 'POST':
    #     return JsonResponse({'version': 1, 'code': RespCode.INVALID_METHOD, 'msg': 'INVALID_METHOD'})
    print(time.strftime('%H:%M:%S ') + "request.get" + str(request.GET))
    if 'uid' not in request.GET :
        return JsonResponse({'code': resp_code.INVALID_ARGUMENT, 'msg': 'INVALID_ARGUMENT'})
    uid=request.GET.get('uid')
    print(uid)
    if not uid.isdigit():
        return JsonResponse({'code': resp_code.INVALID_ARGUMENT, 'msg': 'INVALID_ARGUMENT'})
    if user_recs_col is None:
        return JsonResponse({'code': resp_code.DB_CONN_ERROR, 'msg': 'DB_CONN_ERROR'})
    print(user_recs_col)
    rec_query = {"uid": int(uid)}
    print(rec_query)
    recs = user_recs_col.find(rec_query)[0]['recs']
    # for rec in recs:

    print(recs)
    return JsonResponse({'code': resp_code.SUCCESS, 'msg': 'SUCCESS',"recs:":recs})

    # for x in mydoc:
    #     print(x)
    # if request.POST.get('version') != '1':
    #
