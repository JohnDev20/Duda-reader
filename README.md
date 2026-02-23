# Duda Reader - Aplicativo de Ebooks para Android

Duda Reader é um aplicativo nativo para Android focado na leitura de ebooks nos formatos **PDF, EPUB, HTML e TXT**. Desenvolvido com as tecnologias mais modernas do ecossistema Android, o projeto segue os princípios de **Clean Architecture** e utiliza **Jetpack Compose** para uma interface de usuário fluida e responsiva.

## 🚀 Tecnologias Utilizadas

- **Linguagem:** Kotlin
- **Interface:** Jetpack Compose (Declarativa)
- **Arquitetura:** Clean Architecture (Módulos: app, data, domain, presentation)
- **Injeção de Dependência:** Hilt (Dagger)
- **Banco de Dados:** Room (Persistência local)
- **CI/CD:** Codemagic

## 📁 Estrutura do Projeto

O projeto é dividido em módulos para garantir a separação de responsabilidades:

- `:domain`: Contém as entidades de negócio, interfaces de repositório e casos de uso (Use Cases). Não possui dependências do Android.
- `:data`: Implementação dos repositórios, banco de dados Room e lógica de acesso a arquivos.
- `:presentation`: Camada de interface de usuário com ViewModels e componentes Jetpack Compose.
- `:app`: Módulo principal que orquestra a injeção de dependência e inicialização do aplicativo.

## 🛠️ Configuração do Codemagic CI/CD

Este projeto está configurado para automação de build via Codemagic. O arquivo `codemagic.yaml` na raiz define o pipeline.

### Como funciona:
1. **Gatilho:** Sempre que houver um `push` na branch `main` do GitHub, o build será iniciado automaticamente.
2. **Processo:** O Codemagic provisiona uma máquina Linux, configura o ambiente Android e executa `./gradlew assembleDebug`.
3. **Artefatos:** Após a conclusão, o arquivo `app-debug.apk` estará disponível para download na página de builds do Codemagic.

## 🧪 Checklist de Testes (Smoke Test)

Após gerar o APK, siga estes 10 passos para validar o funcionamento básico:

1. **Instalação:** O APK instala corretamente sem erros.
2. **Abertura:** O app abre e exibe a tela de Biblioteca (vazia inicialmente).
3. **Permissões:** Ao tentar importar, o app solicita permissão de acesso a arquivos.
4. **Importação (PDF):** Selecione um arquivo PDF; ele deve aparecer na lista da biblioteca.
5. **Importação (EPUB):** Selecione um arquivo EPUB; ele deve aparecer na lista.
6. **Categorias:** Mude o status de um livro para "Lendo" e verifique se a categoria é atualizada.
7. **Persistência:** Feche e abra o app; os livros importados devem continuar lá.
8. **Leitura:** Toque em um livro para abrir a tela de leitura.
9. **Destaques:** Tente selecionar um texto e salvar como destaque.
10. **Estabilidade:** Navegue entre as telas rapidamente para garantir que não ocorram crashes.

## ⚠️ Riscos e Mitigações

- **Renderização:** Formatos complexos podem ser lentos. Mitigação: Uso de bibliotecas otimizadas e carregamento em segundo plano.
- **Memória:** Arquivos grandes podem causar erros de memória. Mitigação: Implementação de paginação (Lazy Loading).
- **Armazenamento:** O app copia arquivos para seu diretório interno privado para garantir acesso permanente.
