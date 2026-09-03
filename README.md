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
