/**
 * @author Welomeh, Meloweh
 */

package adris.altoclef.tasks.ArrowMapTests;

public record FeedbackXYZ(CollisionFeedback x,
                          CollisionFeedback y,
                          CollisionFeedback z) {
    public CollisionFeedback getX() {
        return this.x;
    }
    public CollisionFeedback getY() {
        return this.y;
    }
    public CollisionFeedback getZ() {
        return this.z;
    }
}
