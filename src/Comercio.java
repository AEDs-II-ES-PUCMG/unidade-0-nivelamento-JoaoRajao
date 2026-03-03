import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Comercio {
    /** Para inclusão de novos produtos no vetor */
    static final int MAX_NOVOS_PRODUTOS = 10;

    /** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;

    /** Scanner para leitura do teclado */
    static Scanner teclado;

    /** Vetor de produtos cadastrados. Sempre terá espaço para 10 novos produtos a cada execução */
    static Produto[] produtosCadastrados;

    /** Quantidade produtos cadastrados atualmente no vetor */
    static int quantosProdutos;

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa(){
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho(){
        System.out.println("AEDII COMÉRCIO DE COISINHAS");
        System.out.println("===========================");
    }

    /** Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * @return Um inteiro com a opção do usuário.
    */
    static int menu(){
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Procurar e listar um produto");
        System.out.println("3 - Cadastrar novo produto");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        return Integer.parseInt(teclado.nextLine());
    }

    /**
     * Lê os dados de um arquivo texto e retorna um vetor de produtos.
     * Retorna um vetor vazio em caso de problemas com o arquivo.
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Um vetor com os produtos carregados, ou vazio em caso de problemas de leitura.
     */
    static Produto[] lerProdutos(String nomeArquivoDados) {
        Produto[] vetorProdutos;
        try {
            File arquivo = new File(nomeArquivoDados);
            Scanner leitor = new Scanner(arquivo, "ISO-8859-2");

            // Lê a quantidade de produtos na primeira linha
            int quantidade = Integer.parseInt(leitor.nextLine().trim());

            // Aloca o vetor com espaço para os produtos existentes + reserva para novos
            vetorProdutos = new Produto[quantidade + MAX_NOVOS_PRODUTOS];
            quantosProdutos = 0;

            // Lê cada linha e cria o produto correspondente
            for (int i = 0; i < quantidade && leitor.hasNextLine(); i++) {
                String linha = leitor.nextLine().trim();
                if (!linha.isEmpty()) {
                    Produto p = Produto.criarDoTexto(linha);
                    if (p != null) {
                        vetorProdutos[quantosProdutos++] = p;
                    }
                }
            }
            leitor.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado. Iniciando com cadastro vazio.");
            vetorProdutos = new Produto[MAX_NOVOS_PRODUTOS];
            quantosProdutos = 0;
        } catch (Exception e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            vetorProdutos = new Produto[MAX_NOVOS_PRODUTOS];
            quantosProdutos = 0;
        }
        return vetorProdutos;
    }

    /** Lista todos os produtos cadastrados, numerados, um por linha */
    static void listarTodosOsProdutos(){
        cabecalho();
        System.out.println("\nPRODUTOS CADASTRADOS:");
        if (quantosProdutos == 0) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        for (int i = 0; i < quantosProdutos; i++) {
            if (produtosCadastrados[i] != null)
                System.out.println(String.format("%02d - %s", (i + 1), produtosCadastrados[i].toString()));
        }
    }

    /** Localiza um produto no vetor de cadastrados, a partir do nome, e imprime seus dados.
     *  A busca não é sensível ao caso. Em caso de não encontrar o produto, imprime mensagem padrão */
    static void localizarProdutos(){
        cabecalho();
        System.out.print("Digite o nome do produto a localizar: ");
        String nomeBusca = teclado.nextLine();

        // Cria um produto temporário apenas para usar o equals()
        // Como equals() compara apenas a descrição, usamos ProdutoNaoPerecivel como veículo
        Produto chave;
        try {
            chave = new ProdutoNaoPerecivel(nomeBusca, 0.01);
        } catch (Exception e) {
            System.out.println("Nome muito curto para pesquisa (mínimo 3 caracteres).");
            return;
        }

        boolean encontrado = false;
        for (int i = 0; i < quantosProdutos; i++) {
            if (produtosCadastrados[i] != null && produtosCadastrados[i].equals(chave)) {
                System.out.println("Produto encontrado:");
                System.out.println(produtosCadastrados[i].toString());
                encontrado = true;
            }
        }
        if (!encontrado) {
            System.out.println("Produto \"" + nomeBusca + "\" não encontrado.");
        }
    }

    /**
     * Rotina de cadastro de um novo produto: pergunta ao usuário o tipo do produto,
     * lê os dados correspondentes, cria o objeto adequado e inclui no vetor.
     */
    static void cadastrarProduto(){
        cabecalho();
        System.out.println("CADASTRO DE NOVO PRODUTO");
        System.out.println("1 - Produto Não Perecível");
        System.out.println("2 - Produto Perecível");
        System.out.print("Tipo do produto: ");
        int tipo;
        try {
            tipo = Integer.parseInt(teclado.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Tipo inválido.");
            return;
        }

        System.out.print("Descrição: ");
        String desc = teclado.nextLine().trim();

        System.out.print("Preço de custo (ex: 2.50): ");
        double precoCusto;
        try {
            precoCusto = Double.parseDouble(teclado.nextLine().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            System.out.println("Preço inválido.");
            return;
        }

        System.out.print("Margem de lucro (ex: 0.30 para 30%): ");
        double margemLucro;
        try {
            margemLucro = Double.parseDouble(teclado.nextLine().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            System.out.println("Margem inválida.");
            return;
        }

        Produto novoProduto = null;
        try {
            if (tipo == 1) {
                novoProduto = new ProdutoNaoPerecivel(desc, precoCusto, margemLucro);
            } else if (tipo == 2) {
                System.out.print("Data de validade (dd/mm/aaaa): ");
                String dataStr = teclado.nextLine().trim();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate dataDeValidade = LocalDate.parse(dataStr, formatter);
                novoProduto = new ProdutoPerecivel(desc, precoCusto, margemLucro, dataDeValidade);
            } else {
                System.out.println("Tipo de produto inválido.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Erro ao criar produto: " + e.getMessage());
            return;
        }

        // Verifica se ainda há espaço no vetor
        if (quantosProdutos >= produtosCadastrados.length) {
            System.out.println("Capacidade máxima do vetor atingida. Produto não cadastrado.");
            return;
        }

        produtosCadastrados[quantosProdutos++] = novoProduto;
        System.out.println("Produto cadastrado com sucesso!");
        System.out.println(novoProduto.toString());
    }

    /**
     * Salva os dados dos produtos cadastrados no arquivo csv informado. Sobrescreve todo o conteúdo do arquivo.
     * @param nomeArquivo Nome do arquivo a ser gravado.
     */
    public static void salvarProdutos(String nomeArquivo){
        try {
            FileWriter escritor = new FileWriter(nomeArquivo, false); // false = sobrescrever
            escritor.write(quantosProdutos + "\n");
            for (int i = 0; i < quantosProdutos; i++) {
                if (produtosCadastrados[i] != null) {
                    escritor.write(produtosCadastrados[i].gerarDadosTexto() + "\n");
                }
            }
            escritor.close();
            System.out.println("Dados salvos em \"" + nomeArquivo + "\".");
        } catch (IOException e) {
            System.out.println("Erro ao salvar o arquivo: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        teclado = new Scanner(System.in, Charset.forName("ISO-8859-2"));
        nomeArquivoDados = "dadosProdutos.csv";
        produtosCadastrados = lerProdutos(nomeArquivoDados);
        int opcao = -1;
        do {
            opcao = menu();
            switch (opcao) {
                case 1 -> listarTodosOsProdutos();
                case 2 -> localizarProdutos();
                case 3 -> cadastrarProduto();
            }
            pausa();
        } while (opcao != 0);

        salvarProdutos(nomeArquivoDados);
        teclado.close();
    }
}
