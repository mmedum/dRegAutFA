package regaut.strategies;

import regaut.FA;
import regaut.State;

public interface AcceptStrategy {

    /**
     * For setting an accept strategy for nodes in the target FA
     * @param a first unchanged FA
     * @param b second unchanged FA
     * @param aState state from a to check
     * @param bState state from b to check
     * @param newState the new state which should be checked as accept state or not
     * @param target the target FA
     */
    void setAcceptNodes(FA a, FA b, State aState, State bState, State newState, FA target);
}
