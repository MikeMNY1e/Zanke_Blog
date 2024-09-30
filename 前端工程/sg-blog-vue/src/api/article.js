import request from '@/utils/request'

// 查询文章列表
export function articleList(query) {
    return request({
        url: '/article/articleList',
        method: 'get',
        headers: {
          isToken: false
        },
        params: query
    })
}

//查询最热文章
export function hotArticleList() {
    return request({
        url: '/article/hotArticleList',
        headers: {
          isToken: false
        },
        method: 'get'
    })
}

//获取文章详情
export function getArticle(articleId) {
    return request({
        url: '/article/' + articleId,
        headers: {
          isToken: false
        },
        method: 'get'
    })
}

export function updateViewCount(articleId) {

  const token = localStorage.getItem('token');
  const headers = {...request.defaults.headers}

  if (token) {
    headers.token = token;
  }

  return request({
    url: '/article/updateViewCount/' + articleId,
    headers: {
      headers,
      // isToken: false
    },
    method: 'put'
  })

}
