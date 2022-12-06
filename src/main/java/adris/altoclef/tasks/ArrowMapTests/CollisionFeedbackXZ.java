/**
 * @author Welomeh, Meloweh
 */

package adris.altoclef.tasks.ArrowMapTests;

public final class CollisionFeedbackXZ {
    private final CollisionFeedback x, z;
    public CollisionFeedbackXZ(final CollisionFeedback x, final CollisionFeedback z) {
        this.x = x; this.z = z;
    }

    public CollisionFeedback getX() {
        return this.x;
    }

    public CollisionFeedback getZ() {
        return this.z;
    }
}