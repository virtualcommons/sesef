package edu.asu.commons.event;

import edu.asu.commons.net.Identifier;

/**
 * Requests that a Client should display its instructions pane.
 *
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 */
public class ShowInstructionsRequest extends AbstractEvent implements ShowRequest<ShowInstructionsRequest> {

    private static final long serialVersionUID = 3774308614796618926L;

    private final boolean summarized;

    public ShowInstructionsRequest(Identifier id) {
        this(id, false);
    }

    public ShowInstructionsRequest(Identifier id, boolean summarized) {
        super(id);
        this.summarized = summarized;
    }

    /**
     * Returns true if the instructions should be summarized.
     *
     * @return true if the instructions should be summarized, false otherwise.
     */
    public boolean isSummarized() {
        return summarized;
    }

    @Override
    public ShowInstructionsRequest clone(Identifier id) {
        return new ShowInstructionsRequest(id);
    }

}
