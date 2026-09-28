# Simulador de Fórmula 1 com Threads — versão corrigida

Projeto acadêmico em Java para demonstrar concorrência e sincronização usando uma corrida inspirada na Fórmula 1.

## Correções principais

- Removido o `CyclicBarrier` fixo que causava deadlock após um DNF.
- Implementado `Phaser`, permitindo que carros abandonados saiam da sincronização com `arriveAndDeregister()`.
- A corrida consegue chegar até a volta final mesmo com abandonos.
- A classificação parcial agora é impressa a cada volta.
- Safety Car, VSC, bandeira amarela e bandeira vermelha possuem comportamento global.
- Bandeira vermelha interrompe e reinicia a corrida sem travar as Threads.
- Pit stops usam `ReentrantLock` e detectam double stack.
- Penalidades de 5s, 10s, drive-through e stop-and-go são aplicadas.
- O clima pode mudar durante a prova.
- Pneus se desgastam e a estratégia reage à chuva.
- Quebras mecânicas usam a confiabilidade da equipe.
- Erros do piloto usam agressividade, desgaste, chuva e chance individual.
- Max Verstappen possui maior variância de erro conforme a regra especial pedida no projeto, sem reduzir seu talento puro.
- O grid de largada é aleatório, mas ponderado por ritmo do carro e habilidade de classificação.

## Estrutura

```text
src/main/java/f1
├── Main.java
├── corrida
│   ├── Classificacao.java
│   ├── ControleCorrida.java
│   └── Corrida.java
├── eventos
│   ├── Clima.java
│   ├── GerenciadorEventos.java
│   ├── ModoCorrida.java
│   └── TipoEvento.java
├── model
│   ├── Carro.java
│   ├── Equipe.java
│   ├── Penalidade.java
│   ├── Piloto.java
│   └── Pneu.java
└── pitstop
    └── PitStop.java
```

## Requisitos

- Java 17 ou superior.

## IntelliJ

1. Abra a pasta `f1-threads-simulator-corrigido`.
2. Aguarde o IntelliJ reconhecer o `pom.xml`.
3. Abra `src/main/java/f1/Main.java`.
4. Execute o método `main`.

## Terminal sem Maven

Linux/macOS:

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -cp out f1.Main
```

Windows PowerShell:

```powershell
New-Item -ItemType Directory -Force out
$files = Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $files
java -cp out f1.Main
```

## Concorrência usada

- Cada carro implementa `Runnable`.
- A corrida cria 22 objetos `Thread`.
- `Phaser` sincroniza o final de cada volta.
- Um carro que abandona chama `arriveAndDeregister`, então deixa de ser esperado nas voltas seguintes.
- `ReentrantLock` protege o box compartilhado pelos dois pilotos de cada equipe.
- Métodos sincronizados protegem mudanças no estado global da corrida.
- Campos `volatile` garantem visibilidade para estados como clima, bandeiras e abandono.

## Observação

Os overalls são parâmetros do simulador acadêmico e podem ser ajustados sem alterar a arquitetura.

## Atualização de apresentação — tempos e pit stops

- Tempos de volta e classificação agora são exibidos no formato `minuto:segundo.milisegundo`.
  - Exemplo: `96 segundos` aparece como `1:36.000`.
- Cores no console:
  - amarelo: volta mais lenta que a volta anterior;
  - verde: volta mais rápida que a volta anterior;
  - roxo: nova melhor volta absoluta da corrida.
- A saída mostra somente o tempo de volta de cada carro, sem tempos de setores.
- As pausas artificiais da simulação foram reduzidas para a demonstração terminar mais rápido.
- Cada carro tem dois pit stops estratégicos obrigatórios. Um furo ou dano pode causar pit stop extra.
- Charles Leclerc possui um evento específico de batida no muro com chance base de `0,30% por volta`, aumentada em condições de chuva.


## Ritmo visual para apresentação

A versão de apresentação segura cada volta na tela por **1 segundo** antes de liberar as Threads para a próxima volta.
O valor fica em `Main.java`:

```java
private static final long PAUSA_ENTRE_VOLTAS_MS = 1000;
```

Sugestões:
- `1500` = bem devagar, fácil de acompanhar;
- `1000` = recomendado para apresentação;
- `500` = intermediário;
- `0` = sem pausa, útil para testes.

A pausa acontece no avanço do `Phaser`, então todos os carros continuam sincronizados e os tempos simulados da corrida não são alterados.
