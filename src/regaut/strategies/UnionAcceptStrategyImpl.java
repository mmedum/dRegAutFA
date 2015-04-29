package regaut.strategies;

import regaut.FA;
import regaut.State;

/**
 * Created by mark on 4/29/15.
 */
public class UnionAcceptStrategyImpl implements AcceptStrategy {

    @Override
    public void setAcceptNodes(FA a, FA b, State aState, State bState, State newState, FA target) {
        if(a.accept.contains(aState) || b.accept.contains(bState)){
            target.accept.add(newState);
        }
    }
}