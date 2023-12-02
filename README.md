<p align="center">
<img align="center" width="200" height="200" src="src/main/resources/images/music2.png">
<p/>
<h1 align="center">Music FX - MP3 Player</h1> 
<p align="center">
<img alt="Static Badge" src="https://img.shields.io/badge/Java-%23177015?style=for-the-badge&logo=intellijidea&label=Linguagem"> <spam>ㅤㅤ</spam><img alt="Static Badge" src="https://img.shields.io/badge/UFRN--IMD-%23177015?style=for-the-badge&logo=unlicense&label=Licen%C3%A7a">
</p>
A busca eficiente por informações é um desafio central em ciência da computação e desenvolvimento de algoritmos. Nesse contexto, a implementação de estruturas de dados desempenha um papel crucial na organização e recuperação eficaz de dados. Uma dessas estruturas, a Árvore Binária de Busca (ABB), destacase por sua capacidade de otimizar operações fundamentais, como inserção, busca e remoção, proporcionando uma base sólida para diversas aplicações. 

## 🤔 Como funciona?
</p>É bastante simples, quando você clonar o programa, irá acessar a pasta src/arquivos, e encontrar dois arquivos: arquivo1.txt e arquivo2.txt, são nesses arquivos que iremos manusear as instruções e qual árvore desejamos demonstrar.  </p>  

<p> 📁 <b>arquivo1.txt</b> ▸ Local onde você deve inserir os números que a árvore irá conter. É necessário um espaço entre cada número, pode-se ver um exemplo nos arquivos já adicionados.<br />

<p> 📁 <b>arquivo2.txt</b> ▸Local onde você irá por as instruções que o código deve seguir. É necessário por uma instrução por linha, pode ver nos arquivos já inseridos de exemplo.<br />

 
## 💻 Quais são as instruções:

Antes de começar, verifique as instruções que você pode utlizar no código.

*  <b>IMPRIMA</b>: Irá printar a árvore binária no console no formato que você deseja. Existe dois formatos "1", cujo é no formato mais gráfico, e "2", cujo imprime em uma linha todos os nós em sua ordem, como exemplo: `IMPRIMA 1`.
*  <b>ALTURA</b>: Esse metódo irá apenas imprimir a altura da árvore.
*  <b>MEDIANA</b>: retorna o elemento que contém a mediana da ABB. Se a ABB possuir um número par de elementos, retorne o menor dentre os dois elementos medianos
*  <b>ENÉSIMO</b>: retorna o n-ésimo elemento (contando a partir de 1) do percurso em ordem (ordem simétrica) da ABB.
*  <b>MÉDIA</b>: retorna a média aritmética dos nós da árvore que x é a raiz.
*  <b>INSIRA</b>: insere o elemento fornecido dentro da árvore, como exemplo: `INSIRA 20`.
*  <b>BUSCAR</b>: busca um elemento desejado na árvore binária, como exemplo: `BUSCAR 15`.
*  <b>REMOVA</b>: remove, se estiver na árvore, o elemento desejado, caso não esteja, irá retornar que não foi removido, exemplo: `REMOVA 15`.
*  <b>PRE-ORDEM</b>: retorna uma String que contém a sequência de visitação (percorrimento) da ABB em pré-ordem
*  <b>CHEIA</b>: retorna se sua árvore é cheia ou não.
*  <b>COMPLETA</b>: retorna se sua árvore binárioa é completa ou não.
*  <b>POSIÇÃO</b>: retorna a posição ocupada pelo elemento x em um percurso em ordem simétrica na ABB (contando a partir de 1), como exemplo: `POSICAO 15`.

## 💻 Como utilizar?

### É bastante simples, a primeira forma é necessário apenas um navegador.  
Você pode utilizar a plataforma `Replit`, cujo você pode fazer um Fork do nosso <a href="https://replit.com/@Gustavo8911/ABB-1">replit público</a>.

### Utilizar um software para executar a programação.  
É necessário ter `java` e `make` em sua máquina.  

Java - `sudo apt install openjdk-21-jdk`  
Make - `sudo apt install make`

Quando for feito o clone do projeto, é possível compilar usando o comando:  
```bash
cd src && make
```
Após executar o comando acima será gerado um jar, e para executar o programa, utilize o comando.
```bash
java -jar ArvoreBinariaBusca.jar
```
Se preferir, pode baixar o `IntellIJ` e clonar direto do github no software, tendo mais facilidade para manusear o código e executa-lo.  
