package subgraphs;

import gnu.trove.list.TIntList;
import gnu.trove.set.TIntSet;

public interface Subgraph {

    TIntSet getChildren(int cui);

    TIntSet getParents(int cui);

    TIntList allCuis();

}
