## `SDC_CONTROLO`

 O projeto presente consiste na integração de um sistema de gestão de composições ferroviárias, o projeto em questão deve garantir a confiabilidade do sisema, permitindo recuperar da falha de um servidor de modo transparente para o **utilizador**.
***
Este projeto utiliza um sistema de resposta ativa, sendo que as respostas excedentes dos servidores serão descartadas por parte do cliente, resta ainda dizer que o projeto faz uso do **toolkit spread** para realizar o multicasting.

***

## `Estrutura do Projeto`

Este projeto tem por base o trabalho que tem vindo a ser desenvolvido nas aulas de SDC. Como tal grande parte da estrutura mantem-se semelhante, tendo sido modificadas em especifico as componentes relativas à lógica do controlo.

### `Classes`

Para acomodar a nova lógica do projeto foram criadas diversas classes:

* Controlo
 * Esta classe é composta pelas diversas linhas presentes no sistema
* Linha
  * Esta classe trata de definir os critérios de cada linha (tamanho) bem como a sua ocupação, adicionalmente guarda informação sobre todas as composições que se encontram na mesma
* Composição
  * Define a constituição de cada composição indicando a sua identidade e tamanho.

  ## `Tarefas a realizar`
  ### `Minimal Viable Product`
  - [ ] Criação de Classes Auxiliares
  - [ ] Implementação da Lógica do servidor
  - [ ] Implementação de Um Cliente Simples
  ---
  ### `Extras`

  - [ ] (RELATÓRIO) Escolha de utilização de um tipo de replicação especifico
  - [ ] (RELATÓRIO + CÓDIGO) Obtenção de medidas de desempenho
  - [ ] (RELATÓRIO + CÓDIGO) Obtenção de medidas experimentais em diversas máquinas com falhas simuladas
