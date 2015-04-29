package regaut.strategies;

import regaut.FA;
import regaut.State;

/**
 * Created by mark on 4/29/15.
 */
public interface AcceptStrategy {

    void setAcceptNodes(FA a, FA b, State aState, State bState, State newState, FA target);
}
