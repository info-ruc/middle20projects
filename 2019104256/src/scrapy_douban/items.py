import scrapy

class DoubanItem(scrapy.Item):
    movie_name = scrapy.Field()
    movie_director = scrapy.Field()
    movie_writer = scrapy.Field()
    movie_starring = scrapy.Field()
    movie_category = scrapy.Field()
    movie_country = scrapy.Field()
    movie_date = scrapy.Field()
    movie_time = scrapy.Field()
    movie_star = scrapy.Field()
    movie_5score = scrapy.Field()
    movie_4score = scrapy.Field()
    movie_3score = scrapy.Field()
    movie_2score = scrapy.Field()
    movie_1score = scrapy.Field()
    movie_describe = scrapy.Field()
    pass

