import java.awt.image.BufferedImage;
import java.util.List;

public interface ImageGroupFinder {
    /**
     * Finds connected groups in an image.
     * 
     * The groups are sorted in DESCENDING order according to Group's compareTo method.
     * @param image
     * @return connected groups in an image sorted in descending order
     */
    public List<Group> findConnectedGroups(BufferedImage image);
}

/* ADDING NOTES / STEPS
    Here you basically find the connected grops, the groups are sorted in descending order using the compareTO method
    You return the connected gropus in descending order
 */