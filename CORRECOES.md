# Correções realizadas

1. `CyclicBarrier` fixo removido: causava deadlock depois que um piloto abandonava.
2. Sincronização refeita com `Phaser` e `arriveAndDeregister()` para DNFs.
3. Testada conclusão das 58 voltas mesmo com múltiplos abandonos.
4. Classificação parcial agora é impressa a cada volta.
5. Safety Car, VSC, bandeira amarela e bandeira vermelha têm estado global e duração.
6. Bandeira vermelha pausa e reinicia a corrida na barreira de volta sem prender Threads.
7. Safety Car comprime os intervalos do pelotão.
8. Pit stop protegido por `ReentrantLock` e com suporte a double stack.
9. Desgaste e troca de pneus corrigidos, incluindo pneus de chuva.
10. Clima dinâmico integrado à estratégia e ao ritmo.
11. Falhas mecânicas passaram a depender da confiabilidade da equipe.
12. Erros dos pilotos passaram a depender de agressividade, chuva e desgaste.
13. Verstappen mantém alto desempenho, mas possui maior variância de erro conforme pedido.
14. Penalidades de 5s, 10s, drive-through e stop-and-go são executadas corretamente.
15. Drive-through/stop-and-go pendentes na última volta recebem conversão de tempo na chegada.
16. Grid de largada usa três tentativas de classificação, ritmo e aleatoriedade.
17. Resultado final separa classificados e DNFs.
18. Código validado com Java 17 (`javac --release 17`).
19. Foram executadas múltiplas simulações completas para verificar ausência de deadlock.
