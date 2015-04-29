package regaut;

public class Test
{
    public static FA test(){
        Alphabet alphabet = new Alphabet('0','1');

        FA m1 = new FA();
        m1.alphabet = alphabet;

        FA m2 = new FA();
        m2.alphabet = alphabet;

        State a = new State("A");
        State b = new State("B");
        State c = new State("C");
        m1.states.add(a);
        m1.states.add(b);
        m1.states.add(c);

        State r = new State("R");
        State s = new State("S");
        m2.states.add(r);
        m2.states.add(s);

        m1.initial = a;
        m2.initial = r;

        m1.accept.add(c);
        m2.accept.add(s);

        m1.transitions.put(new StateSymbolPair(a,'0'),b);
        m1.transitions.put(new StateSymbolPair(a,'1'),a);
        m1.transitions.put(new StateSymbolPair(b,'0'),b);
        m1.transitions.put(new StateSymbolPair(b,'1'),c);
        m1.transitions.put(new StateSymbolPair(c,'0'),b);
        m1.transitions.put(new StateSymbolPair(c,'1'),a);

        m2.transitions.put(new StateSymbolPair(r,'0'),s);
        m2.transitions.put(new StateSymbolPair(r,'1'),s);
        m2.transitions.put(new StateSymbolPair(s,'0'),r);
        m2.transitions.put(new StateSymbolPair(s, '1'), r);

        return m1.intersection(m2);
    }

    public static void main(String[] args){
        System.out.println(test().toDot());
    }
}
