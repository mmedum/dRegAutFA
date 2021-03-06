package regaut;

import regaut.strategies.AcceptStrategy;
import regaut.strategies.DifferenceAcceptStrategyImpl;
import regaut.strategies.IntersectionAcceptStrategyImpl;
import regaut.strategies.UnionAcceptStrategyImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Deterministic finite state automaton. [Martin, Def. 2.11]
 */
public class FA implements Cloneable {
    
    /** 
     * Set of {@link State} objects (Q). 
     */
    public Set<State> states;
    
    /** 
     * The automaton alphabet (<img src="http://cs.au.dk/~amoeller/RegAut/Sigma.gif" alt="sigma">). 
     */
    public Alphabet alphabet;
    
    /**
     * Initial state (q<sub>0</sub>). Member of {@link #states}. 
     */
    public State initial;
    
    /** 
     * Accept states (A). Subset of {@link #states}. 
     */
    public Set<State> accept;
    
    /**
     * Transition function (<img src="http://cs.au.dk/~amoeller/RegAut/delta.gif" alt="delta">). 
     * This is a map from pairs of states and alphabet symbols to states
     * (<img src="http://cs.au.dk/~amoeller/RegAut/delta.gif" alt="delta">: 
     * Q<img src="http://cs.au.dk/~amoeller/RegAut/times.gif" 
     *      alt="x"><img src="http://cs.au.dk/~amoeller/RegAut/Sigma.gif" alt="sigma">
     * <img src="http://cs.au.dk/~amoeller/RegAut/rightarrow.gif" alt="-&gt;"> Q).
     */
    public Map<StateSymbolPair,State> transitions;
    
    /**
     * Checks that this automaton is well-defined.
     * In particular, this method checks that the transition function is total.
     * This method should be invoked after each <tt>FA</tt> operation during testing.
     * @return this automaton
     * @exception AutomatonNotWellDefinedException if this automaton is not well-defined
     */
    public FA checkWellDefined() throws AutomatonNotWellDefinedException {
        if (states==null || alphabet==null || alphabet.symbols==null ||
            initial==null || accept==null || transitions==null)
            throw new AutomatonNotWellDefinedException("invalid null pointer");
        if (!states.contains(initial))
            throw new AutomatonNotWellDefinedException("the initial state is not in the state set");
        if (!states.containsAll(accept))
            throw new AutomatonNotWellDefinedException("not all accept states are in the state set");
        for (State s : states) 
            for (char c : alphabet.symbols) {
                if (c==NFA.LAMBDA)
                    throw new AutomatonNotWellDefinedException("lambda transition appears in transitions");
                State s2 = transitions.get(new StateSymbolPair(s, c));
                if (s2==null)
                    throw new AutomatonNotWellDefinedException("transition function is not total");
                if (!states.contains(s2))
                    throw new AutomatonNotWellDefinedException("there is a transition to a state that is not in state set");
            }
        for (StateSymbolPair sp : transitions.keySet()) {
            if (!states.contains(sp.state))
                throw new AutomatonNotWellDefinedException("transitions refer to a state not in the state set");
            if (!alphabet.symbols.contains(sp.symbol))
                throw new AutomatonNotWellDefinedException("non-alphabet symbol appears in transitions");
        }
        return this;
    }
    
    /**
     * Constructs an uninitialized FA. 
     * <tt>states</tt> and <tt>accept</tt> are set to empty sets,
     * <tt>transitions</tt> is set to an empty map.
     */
    public FA() {
        states = new HashSet<State>();
        accept = new HashSet<State>();
        transitions = new HashMap<StateSymbolPair,State>();
    }
    
    /** 
     * Constructs a new FA consisting of one reject state. 
     * @param a automaton alphabet
     */
    public FA(Alphabet a) {
        states = new HashSet<State>();
        accept = new HashSet<State>();
        alphabet = a;
        
        // make a state
        State s = new State();
        states.add(s);
        initial = s;
        
        // add a loop transition for each alphabet symbol
        transitions = new HashMap<StateSymbolPair,State>();
        for (char c : alphabet.symbols) 
            transitions.put(new StateSymbolPair(s, c), s);
    }
    
    /** 
     * Clones this automaton. 
     */
    @Override
    public Object clone() {
        FA f;
        try {
            f = (FA) super.clone(); // always clone using super.clone()
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        f.states = new HashSet<State>();
        f.accept = new HashSet<State>();
        f.transitions = new HashMap<StateSymbolPair,State>();
        Map<State,State> m = new HashMap<State,State>(); // map from old states to new states
        for (State p : states) {
            State s = (State)p.clone();
            f.states.add(s);
            m.put(p, s);
            if (accept.contains(p))
                f.accept.add(s);
        }
        f.initial = m.get(initial);
        for (Map.Entry<StateSymbolPair,State> e : transitions.entrySet()) {
            StateSymbolPair ssp = e.getKey();
            State q = e.getValue();
            f.transitions.put(new StateSymbolPair(m.get(ssp.state), ssp.symbol), m.get(q));
        }
        return f;
    }
    
    /** 
     * Returns <a href="http://www.graphviz.org/" target="_top">Graphviz Dot</a> 
     * representation of this automaton. 
     * (To convert a dot file to postscript, run '<tt>dot -Tps -o file.ps file.dot</tt>'.)
     */
    public String toDot() {
        StringBuffer b = new StringBuffer("digraph Automaton {\n");
        b.append("  rankdir = LR;\n");
        Map<State,Integer> id = new HashMap<State,Integer>();
        for (State s : states) 
            id.put(s, Integer.valueOf(id.size()));
        for (State s : states) {
            b.append("  ").append(id.get(s));
            if (accept.contains(s))
                b.append(" [shape=doublecircle,label=\""+s.name+"\"];\n");
            else
                b.append(" [shape=circle,label=\""+s.name+"\"];\n");
            if (s==initial) {
                b.append("  in [shape=plaintext,label=\"\"];\n");
                b.append("  in -> ").append(id.get(s)).append(";\n");
            }
        }
        for (Map.Entry<StateSymbolPair,State> e : transitions.entrySet()) {
            StateSymbolPair ssp = e.getKey();
            State q = e.getValue();
            b.append("  ").append(id.get(ssp.state)).append(" -> ").append(id.get(q));
            b.append(" [label=\"");
            char c = ssp.symbol.charValue();
            if (c>=0x21 && c<=0x7e && c!='\\' && c!='%')
                b.append(c);
            else {
                b.append("\\u");
                String s = Integer.toHexString(c);
                if (c<0x10)
                    b.append("000").append(s);
                else if (c<0x100)
                    b.append("00").append(s);
                else if (c<0x1000)
                    b.append("0").append(s);
                else
                    b.append(s);
            }
            b.append("\"];\n");
        }
        return b.append("}\n").toString();
    }
    
    /** 
     * Returns number of states in this automaton. 
     */
    public int getNumberOfStates() {
        return states.size();
    }
    
    /** 
     * Sets a transition in the transition function. 
     * <img src="http://cs.au.dk/~amoeller/RegAut/delta.gif" alt="delta">(q,c)=p
     */
    public void setTransition(State q, char c, State p) {
        transitions.put(new StateSymbolPair(q, c), p);
    }
    
    /**
     * Looks up transition in transition function.
     * @return <img src="http://cs.au.dk/~amoeller/RegAut/delta.gif" alt="delta">(q,c) 
     * @exception IllegalArgumentException if <tt>c</tt> is not in the alphabet
     */
    public State delta(State q, char c) throws IllegalArgumentException {
        if (!alphabet.symbols.contains(c))
            throw new IllegalArgumentException("symbol '"+c+"' not in alphabet");
        return transitions.get(new StateSymbolPair(q, c));
    }
    
    /**
     * Performs transitions in extended transition function. [Martin, Def. 2.12]
     * @return <img src="http://cs.au.dk/~amoeller/RegAut/delta.gif" alt="delta">*(q,s) 
     * @exception IllegalArgumentException if a symbol in <tt>s</tt> is not in the alphabet
     */
    public State deltaStar(State q, String s) throws IllegalArgumentException {
        // (Using recursion instead of iteration would have been closer to
        // the mathematical definition, but this code is simpler...)
        for (char c : s.toCharArray())
            q = delta(q,c);
        return q;
    }
    
    /**
     * Runs the given string on this automaton. [Martin, Def. 2.14]
     * @param s a string
     * @return true iff the string is accepted
     * @exception IllegalArgumentException if a symbol in <tt>s</tt> is not in the alphabet
     */
    public boolean accepts(String s) throws IllegalArgumentException {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Pair of states. Used in product construction and in construction of regular expression. 
     */
    static class StatePair {
        
        State s1, s2;
        
        /** 
         * Constructs a new pair. 
         */
        StatePair(State s1, State s2) {
            this.s1 = s1;
            this.s2 = s2;
        }
        
        /** 
         * Checks whether two pairs are equal. 
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StatePair))
                return false;
            StatePair ss = (StatePair) obj;
            return s1==ss.s1 && s2==ss.s2;
        }
        
        /** 
         * Computes hash code for this object. 
         */
        @Override
        public int hashCode() {
            return s1.hashCode()*3 + s2.hashCode()*2;
        }
    }

    /** 
     * Converts this automaton into an equivalent {@link RegExp} regular expression. [Martin, Th. 3.30] 
     */
    public RegExp toRegExp() {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Constructs a new automaton that accepts the complement of the language of this automaton. 
     * The input automaton is unmodified.
     */
    public FA complement() {
        FA f = (FA) clone();
        Set<State> s = new HashSet<State>();
        s.addAll(f.states);
        s.removeAll(f.accept);
        f.accept = s;
        return f;
    }
    
    /** 
     * Finds the set of states that are reachable from the initial state. [Martin, Exercise 2.27(b)] 
     */
    public Set<State> findReachableStates() { 
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Constructs a new automaton with the same language as this automaton but without unreachable states. 
     * The input automaton is unmodified.
     */
    public FA removeUnreachableStates() {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Constructs a new minimal automaton with the same language as this automaton. [Martin, Sec. 2.6] 
     * The input automaton is unmodified.
     * Note: this textbook algorithm is simple to understand but not very efficient 
     * compared to other existing algorithms.
     */
    public FA minimize() { 
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Checks whether the language of this automaton is finite. [Martin, Example 2.34] 
     */
    public boolean isFinite() { 
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Checks whether the language of this automaton is empty. [Martin, Example 2.34] 
     */
    public boolean isEmpty() {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Checks whether the language of this automaton is a subset of the language of the given automaton. [Martin, Exercise 2.27(g)] 
     */
    public boolean subsetOf(FA f) {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Computes hash code for this object. 
     * (When {@link #equals(Object)} is implemented, <tt>hashCode</tt> must also be there.)
     */
    @Override
    public int hashCode() {
        return getNumberOfStates(); // a very simple but valid hash code
    }
    
    /** 
     * Checks whether the language of this automaton is equal to the language of the given automaton. 
     */
    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /** 
     * Returns a shortest string that is accepted by this automaton. 
     * @return a (not necessarily unique) shortest example string, null if the language of this automaton is empty
     */
    public String getAShortestExample() { 
        throw new UnsupportedOperationException("method not implemented yet!");
    }
    
    /**
     * Constructs a new automaton whose language is the intersection of the language of this automaton
     * and the language of the given automaton. [Martin, Th. 2.15]
     * The input automata are unmodified.
     * @exception IllegalArgumentException if the alphabets of <tt>f</tt> and this automaton are not the same
     */
    public FA intersection(FA f) throws IllegalArgumentException {
        return product(f, new IntersectionAcceptStrategyImpl());
    }
    
    /**
     * Constructs a new automaton whose language is the union of the language of this automaton
     * and the language of the given automaton. [Martin, Th. 2.15]
     * The input automata are unmodified.
     * @exception IllegalArgumentException if the alphabets of <tt>f</tt> and this automaton are not the same
     * @see NFA#union
     */
    public FA union(FA f) throws IllegalArgumentException {
        return product(f, new UnionAcceptStrategyImpl());
    }
    
    /**
     * Constructs a new automaton whose language is equal to the language of this automaton
     * minus the language of the given automaton. [Martin, Th. 2.15]
     * The input automata are unmodified.
     * @exception IllegalArgumentException if the alphabets of <tt>f</tt> and this automaton are not the same
     */
    public FA minus(FA f) throws IllegalArgumentException {
        return product(f, new DifferenceAcceptStrategyImpl());
    }

    /**
     * Method for setting up the the product of two FA and based on the accept stategy
     * set accept states correctly, always builds all transactions from the new states
     * @param other the FA which should be used for the product creation
     * @param acceptStrategy strategy choosen for the accept state check
     * @return the product FA with the used accept strategy used
     */
    private FA product(FA other, AcceptStrategy acceptStrategy){
        FA result = new FA();
        if(!this.alphabet.equals(other.alphabet)){
            throw new IllegalArgumentException("Not the same alphabet");
        }
        result.alphabet = this.alphabet;
        // Used for transactions
        Map<StatePair, State> pairedStates = new HashMap<>();

        //Create all the new states and set the accept states
        for(State thisState : this.states){
            for(State otherState : other.states){
                State pairState = new State("(" + thisState.name + "." + otherState.name + ")");
                result.states.add(pairState);
                pairedStates.put(new StatePair(thisState, otherState), pairState);
                acceptStrategy.setAcceptNodes(this, other, thisState, otherState, pairState, result);
            }
        }

        // Build the transactions between all the new states
        for(StatePair pair : pairedStates.keySet()){
            for(Character c : this.alphabet.symbols){
                StateSymbolPair from = new StateSymbolPair(pairedStates.get(pair), c);
                State to = pairedStates.get(new StatePair(this.delta(pair.s1, c), other.delta(pair.s2, c)));
                result.transitions.put(from, to);
            }
        }

        // Set the initial state for the result FA
        result.initial = pairedStates.get(new StatePair(this.initial, other.initial));
        return result;
    }
}
