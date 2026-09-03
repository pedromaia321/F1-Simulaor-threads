package f1;

import f1.corrida.Classificacao;
import f1.corrida.ControleCorrida;
import f1.corrida.Corrida;
import f1.model.Carro;
import f1.model.Equipe;
import f1.model.Piloto;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        ControleCorrida controle = new ControleCorrida(58);

        Equipe mercedes = new Equipe("Mercedes", 99, 97, 96, 95);
        Equipe mclaren = new Equipe("McLaren", 97, 96, 96, 98);
        Equipe ferrari = new Equipe("Ferrari", 96, 94, 92, 91);
        Equipe redBull = new Equipe("Red Bull", 91, 86, 95, 99);
        Equipe alpine = new Equipe("Alpine", 84, 90, 87, 86);
        Equipe racingBulls = new Equipe("Racing Bulls", 83, 91, 86, 88);
        Equipe haas = new Equipe("Haas", 81, 90, 82, 83);
        Equipe audi = new Equipe("Audi", 79, 89, 84, 84);
        Equipe williams = new Equipe("Williams", 77, 87, 84, 84);
        Equipe astonMartin = new Equipe("Aston Martin", 73, 75, 85, 82);
        Equipe cadillac = new Equipe("Cadillac", 70, 72, 79, 78);

        List<Carro> carros = new ArrayList<>();

        adicionar(carros, controle, mercedes,
                piloto("Kimi Antonelli", 98, 98, 98, 95, 94, 94, 97, 91, 96, 0.0045),
                piloto("George Russell", 96, 96, 96, 94, 96, 95, 96, 88, 95, 0.0040));

        adicionar(carros, controle, mclaren,
                piloto("Lando Norris", 97, 98, 98, 96, 95, 97, 97, 90, 98, 0.0045),
                piloto("Oscar Piastri", 93, 94, 94, 93, 93, 92, 94, 88, 95, 0.0050));

        adicionar(carros, controle, ferrari,
                piloto("Lewis Hamilton", 95, 96, 95, 96, 95, 98, 95, 92, 96, 0.0050),
                piloto("Charles Leclerc", 95, 97, 97, 95, 94, 95, 94, 94, 94, 0.0055));

        adicionar(carros, controle, redBull,
                piloto("Max Verstappen", 97, 99, 98, 99, 98, 99, 91, 99, 96, 0.0125),
                piloto("Isack Hadjar", 85, 86, 85, 84, 84, 83, 85, 90, 84, 0.0070));

        adicionar(carros, controle, alpine,
                piloto("Pierre Gasly", 88, 89, 88, 87, 87, 88, 89, 89, 88, 0.0060),
                piloto("Franco Colapinto", 82, 83, 82, 82, 80, 81, 82, 92, 81, 0.0085));

        adicionar(carros, controle, racingBulls,
                piloto("Liam Lawson", 83, 84, 83, 83, 82, 82, 83, 93, 82, 0.0080),
                piloto("Arvid Lindblad", 81, 82, 82, 81, 79, 80, 80, 91, 80, 0.0090));

        adicionar(carros, controle, haas,
                piloto("Esteban Ocon", 84, 84, 83, 84, 85, 84, 86, 88, 85, 0.0065),
                piloto("Oliver Bearman", 85, 86, 85, 86, 83, 83, 84, 91, 83, 0.0075));

        adicionar(carros, controle, audi,
                piloto("Nico Hulkenberg", 86, 86, 87, 85, 86, 86, 88, 86, 87, 0.0060),
                piloto("Gabriel Bortoleto", 84, 85, 84, 84, 82, 83, 84, 88, 83, 0.0072));

        adicionar(carros, controle, williams,
                piloto("Carlos Sainz", 88, 89, 89, 88, 88, 88, 90, 86, 89, 0.0058),
                piloto("Alex Albon", 87, 88, 87, 87, 86, 87, 88, 87, 87, 0.0062));

        adicionar(carros, controle, astonMartin,
                piloto("Fernando Alonso", 91, 92, 93, 93, 94, 97, 92, 95, 91, 0.0065),
                piloto("Lance Stroll", 79, 80, 79, 78, 79, 80, 79, 85, 80, 0.0095));

        adicionar(carros, controle, cadillac,
                piloto("Sergio Perez", 83, 84, 83, 84, 85, 84, 83, 86, 84, 0.0075),
                piloto("Valtteri Bottas", 84, 85, 85, 83, 84, 86, 86, 82, 86, 0.0068));

        List<Carro> grid = Classificacao.realizar(carros);
        Corrida corrida = new Corrida(grid, controle);
        corrida.iniciar();
    }

    private static Piloto piloto(String nome, int overall, int velocidade,
                                 int qualificacao, int ultrapassagem, int defesa,
                                 int chuva, int consistencia, int agressividade,
                                 int gerenciamentoPneus, double chanceErro) {
        return new Piloto(nome, overall, velocidade, qualificacao, ultrapassagem,
                defesa, chuva, consistencia, agressividade, gerenciamentoPneus,
                chanceErro);
    }

    private static void adicionar(List<Carro> carros,
                                  ControleCorrida controle,
                                  Equipe equipe,
                                  Piloto p1,
                                  Piloto p2) {
        Carro c1 = new Carro(p1, equipe, controle);
        Carro c2 = new Carro(p2, equipe, controle);

        carros.add(c1);
        carros.add(c2);

        controle.adicionarCarro(c1);
        controle.adicionarCarro(c2);
    }
}
