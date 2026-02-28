## language: pt
#Funcionalidade: Criação de Categoria
#  Como um administrador do sistema
#  Eu quero poder criar novas categorias
#  Para organizar os produtos da loja
#
#  Cenário: Criar uma nova categoria com sucesso
#    Dado que quero criar uma categoria com o nome "Eletrônicos"
#    Quando enviar a requisição de criação de categoria
#    Então devo receber uma mensagem de "Categoria criada com sucesso"
#    E o status da resposta deve ser 201
#
#  Cenário: Atualizar uma categoria existente com sucesso
#    Dado que existe uma categoria cadastrada com o nome "BEBIDAS"
#    E quero atualizar o nome para "LANCHES"
#    Quando enviar a requisição de atualização de categoria
#    Então devo receber uma mensagem de "Categoria atualizada com sucesso"
#    E o status da resposta deve ser 200
#
#  Cenário: Excluir uma categoria existente com sucesso
#    Dado que existe uma categoria cadastrada com id 1
#    Quando enviar a requisição de exclusão de categoria
#    Então devo receber uma mensagem de "Categoria excluída com sucesso"
#    E o status da resposta deve ser 200
#
#  Cenário: Listar todas as categorias com paginação com sucesso
#    Dado que existem 2 categorias cadastradas no sistema
#    Quando solicitar a listagem de categorias com os parâmetros:
#      | limit  | 10 |
#      | offset | 0  |
#      | page   | 0  |
#    Então devo receber uma lista com 2 categorias
#    E o status da resposta deve ser 200
#
#  Cenário: Listar categorias com página vazia
#    Dado que não existem categorias cadastradas no sistema
#    Quando solicitar a listagem de categorias com os parâmetros:
#      | limit  | 10 |
#      | offset | 0  |
#      | page   | 0  |
#    Então devo receber uma lista vazia
#    E o status da resposta deve ser 200
#
#  Cenário: Buscar produtos de uma categoria existente com sucesso
#    Dado que existe uma categoria com id 1 que possui produtos cadastrados
#    Quando solicitar a listagem de produtos da categoria
#    Então devo receber uma lista de produtos da categoria
#    E o status da resposta deve ser 200
#
#  Cenário: Buscar produtos de uma categoria sem produtos cadastrados
#    Dado que existe uma categoria com id 2 sem produtos cadastrados
#    Quando solicitar a listagem de produtos da categoria
#    Então devo receber uma lista vazia de produtos
#    E o status da resposta deve ser 200
#
#  Cenário: Buscar produtos de uma categoria inexistente
#    Dado que não existe uma categoria com id 999
#    Quando solicitar a listagem de produtos da categoria
#    Então devo receber uma resposta vazia
#    E o status da resposta deve ser 200
