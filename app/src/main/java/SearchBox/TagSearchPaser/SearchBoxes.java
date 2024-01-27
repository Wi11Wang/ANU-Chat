package SearchBox.TagSearchPaser;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import Moment.Tag.Tag;
import SearchBox.DataStructure.RBTree;
import SearchBox.TagToken.Tokenizer;

public class SearchBoxes {

    // Input
    String inputContent;

    // Search Box
    OrBox orBox;
    AndBox andBox;
    PlaintextBox plaintextBox;
    RBTree<Tag> tagTree;

    // Result
    ArrayList<String> finalMomentIdList = new ArrayList<>();

    public SearchBoxes() {
        updateRedBlackTree();
    }

    public SearchBoxes(String inputContent) {
        this.inputContent = inputContent;
        updateRedBlackTree();
        updateSearchMomentIDs();
    }

    private void updateRedBlackTree() {
        /**
         * use tags to build a red black tree from firebase with nodes of tags
         */
        FirebaseDatabase.getInstance().getReference().child("Tags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Tag> tags = new ArrayList<>();
                for (DataSnapshot tagSnapshot : snapshot.getChildren()) {
                    ArrayList<String> momentIDs = new ArrayList<>();
                    for (DataSnapshot momentIDSnapshot : tagSnapshot.getChildren()) {
                        momentIDs.add(momentIDSnapshot.getKey());
                    }
                    tags.add(new Tag(tagSnapshot.getKey().toLowerCase(), momentIDs));
                    Log.e("element added to RBTree", new Tag(tagSnapshot.getKey(), momentIDs).toString());
                }
                tagTree = buildRBTree(tags);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void updateSearchContent(String inputContent) {
        this.inputContent = inputContent;
        updateSearchMomentIDs();
    }

    public void updateSearchMomentIDs() {
        if (inputContent.equals("")) {
//            searchView.requestFocusFromTouch();
        } else {

            /**
             * start searching!
             * initialize, get tokenized searching content, parse...
             */
            Tokenizer tokens = new Tokenizer(inputContent);
            SearchParser parser = new SearchParser(tokens);
            String firstTagStr = parser.getFirstTag();
            Tag firstTag = new Tag(firstTagStr);
            andBox = parser.getAndBox();
            orBox = parser.getOrBox();
            plaintextBox = parser.getPlainTextBox();
            ArrayList<String > firstTagMomentIDs = simpleCaseSearching(firstTag, tagTree);

            /**
             search first tag
             get all the ids of the first tag
             A = a set of momentIds corresponding to firstTag
             */
            if (andBox.size()==0 && orBox.size()==0 && plaintextBox.size()==0) {
                finalMomentIdList = firstTagMomentIDs;
            }
            /**
             search only or tags and first tag
             get the union list of Moment Ids
             B = a set of union momentIds corresponding to all orTags in orBox and first tag
             */
            else if(andBox.size()==0 && orBox.size()>0 && plaintextBox.size()==0) {
                ArrayList<String> orBoxAllMomentIDs = onlyOrCaseSearching(orBox, tagTree);
                firstTagMomentIDs.addAll(orBoxAllMomentIDs);
                finalMomentIdList = firstTagMomentIDs;
            }
            /**
             search only and tags and first tag
             get the intersection list of Moment Ids
             C = a set of intersect momentIds corresponding to all andTags in andBox and first tag
             */
            else if(andBox.size()>0 && orBox.size()==0 && plaintextBox.size()==0) {
//                ArrayList<String> andBoxAllMomentIDs = onlyAndCaseSearching(andBox, tagTree);
//                firstTagMomentIDs.retainAll(andBoxAllMomentIDs);
//                finalMomentIdList = firstTagMomentIDs;
                finalMomentIdList = stepByStepIntersectSets(firstTagMomentIDs, tagTree, andBox);
            }
            /**
             * search and + or tags and first tag
             * complex senario: select the intersection ones of union ones
             * A + B - C = A + (B - C) != A - (C + B) -> (A union B) intersect C OR A union (B intersect C)
             * Thus, first calculate the intersection between B nad C, then output the union of A and (B - C)
             */
            else if(andBox.size()>0 && orBox.size()>0 && plaintextBox.size()==0) {
//                ArrayList<String> andBoxAllMomentIDs = onlyAndCaseSearching(andBox, tagTree);
//                ArrayList<String> orBoxAllMomentIDs = onlyOrCaseSearching(orBox, tagTree);
//                orBoxAllMomentIDs.retainAll(andBoxAllMomentIDs);
//                firstTagMomentIDs.addAll(orBoxAllMomentIDs);
//                finalMomentIdList = firstTagMomentIDs;
                ArrayList<String> orBoxAllMomentIDs = onlyOrCaseSearching(orBox, tagTree);
                firstTagMomentIDs.addAll(orBoxAllMomentIDs);
                finalMomentIdList = stepByStepIntersectSets(firstTagMomentIDs, tagTree, andBox);
            }
        }
    }

    /**
     * return an arraylist of search result using tag
     * @return a list of moment IDs
     */
    public ArrayList<String> returnSearchResult() {
        return finalMomentIdList;
    }

    /**
     * It will implement the searching function when only one tag input.
     * 1. get tag String
     * 2. use tag string to get tag ID
     * 3. use tag ID to get an array list of moment ID
     * @param tag input tag for searching
     * @return a list of corresponding moment ID that meets the tag features
     */
    public ArrayList<String> simpleCaseSearching(Tag tag, RBTree<Tag> tagRBTree) {
        RBTree<Tag>.Node<Tag> foundNode = tagRBTree.search(tag);
        ArrayList<String> output = foundNode.getValue().getMomentIds(); //FIREBASE - use tag string to get tag ID to return a list of momentIDS
        return output;
    }

    /**
     * Author: Xinyi Li
     *
     * It will implement the searching function when only "&" relation among/between tags.
     * @param orBox all input tags for searching (without plain text)
     * @return a list of corresponding moment ID that meets the tag features
     */
    public ArrayList<String> onlyOrCaseSearching(OrBox orBox, RBTree<Tag> tagRBTree) {
        ArrayList<Tag> andBoxElementTag = new ArrayList<>();
        ArrayList<String> andBoxAllMomentIDs = new ArrayList<>();
        ArrayList<String> orBoxElementStr = orBox.getOrList(); // Get an array list of Tag strings from addBox
        for (String orTag: orBoxElementStr) {
            andBoxElementTag.add(new Tag(orTag)); // Convert every element into Tag class
        }
        /**
         * Get corresponding moment IDs of every andTags like simple case above
         * 1. get tag String
         * 2. use tag string to get tag ID
         * 3. use tag ID to get an array list of moment ID
         */
        for (Tag andTags: andBoxElementTag) {
            ArrayList<String> thisTagAllMomentIDs = new ArrayList<>();
            RBTree<Tag>.Node<Tag> findNode = tagRBTree.find(tagRBTree.root, andTags); //FIREBASE - use tag string to get tag ID to get an array list of moment ID
            ArrayList<String> momentIDs = findNode.getValue().getMomentIds();
            for (String id: momentIDs) {
                thisTagAllMomentIDs.add(id);
            }
            andBoxAllMomentIDs.addAll(thisTagAllMomentIDs);
        }
        return andBoxAllMomentIDs;
    }

    public ArrayList<String> stepByStepIntersectSets(ArrayList<String> setAfterUnion, RBTree<Tag> rbtree, AndBox andBoxLocal) {
        for (String andTagStr: andBoxLocal.getAndList()) {
            Tag andTag = new Tag(andTagStr);
            RBTree<Tag>.Node<Tag> foundAndNode = rbtree.search(andTag);
            ArrayList<String> toIntersect = foundAndNode.getValue().getMomentIds();
            setAfterUnion.retainAll(toIntersect);
        }
        return setAfterUnion;
    }
    /**
     *
     * It will implement the searching function when only "|" relation among/between tags.
     * @param andBox all input tags for searching (without plain text)
     * @return a list of corresponding moment ID that meets the tag features
     */
    public ArrayList<String> onlyAndCaseSearching(AndBox andBox, RBTree<Tag> tagRBTree) {
        ArrayList<Tag> andBoxElementTag = new ArrayList<>();
        ArrayList<String> andBoxAllMomentIDs = new ArrayList<>();

        ArrayList<String> andBoxElementStr = andBox.getAndList(); // Get an array list of Tag strings from addBox

        for (String andTag: andBoxElementStr) {
            andBoxElementTag.add(new Tag(andTag)); // Convert every element into Tag class
        }
        for (Tag andTags: andBoxElementTag) {
            ArrayList<String> thisTagAllMomentIDs = new ArrayList<>();
            RBTree<Tag>.Node<Tag> findNode = tagRBTree.find(tagRBTree.root, andTags); //FIREBASE - use tag string to get tag ID to get an array list of moment ID
            ArrayList<String> momentIDs = findNode.getValue().getMomentIds();
            for (String id: momentIDs) {
                thisTagAllMomentIDs.add(id);
            }
            andBoxAllMomentIDs.retainAll(thisTagAllMomentIDs);
        }
        return andBoxAllMomentIDs;
    }

    /**
     * Author: Xinyi Li
     *
     * Build an RBTree by inserting all moments stored in a local file
     * @param allTags all tags stored in a local file
     * @return an RBTree
     */
    private RBTree<Tag> buildRBTree(ArrayList<Tag> allTags) {
        RBTree<Tag> tree = new RBTree<>();
        for (Tag tags: allTags) tree.insert(tags);
        return tree;
    }

    /**
     * Author: Xinyi Li
     * It will implement the searching function when only "&" and "|" relation both exist among/between tags.
     * @param tag all input tags for searching (without plain text)
     * @return a list of corresponding moment ID that meets the tag features
     */
    public ArrayList<String> mixedCaseSearching(Tag tag, RBTree<Tag> tagRBTree) {
        return null;
    }

    /**
     * Author: Xinyi Li
     * It will implement the searching function when only "&" and "|" and plain text relation both exist among/between tags.
     * @param tag all input tags for searching (without plain text)
     * @return a list of corresponding moment ID that meets the tag features
     */
    public Tag advancedMixedCaseSearching(Tag tag, RBTree<Tag> tagRBTree) {
        /**
         * 1. first check if the context contains the plain text
         * 2. then check the starting index X of the string. If X-1 is "#" then, do not return it.
         * 3. return all the other moments
         */
        return null;
    }
}
