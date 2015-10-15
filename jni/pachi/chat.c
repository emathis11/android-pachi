#include <assert.h>
#include <math.h>
#include <stdio.h>
#include <sys/types.h>
#include <stdint.h>

#define DEBUG

#include "debug.h"
#include "chat.h"
#include "random.h"

#define MAX_CHAT_PATTERNS 500

static struct chat {
	double minwin;
	double maxwin;
	char from[20];
	char regex[100];
	char reply[300]; // in printf format with one param (100*winrate)

	bool displayed;
	bool match;
} *chat_table;

static char default_reply[] = "I know all those words, but that sentence makes no sense to me";
static char not_playing[] = "I'm winning big without playing";

/* Read the chat file, a sequence of lines of the form:
 * minwin;maxwin;from;regex;reply
 * Set minwin, maxwin to -1.0 2.0 for answers to chat other than winrate.
 * Set from as one space for replies to anyone.
 * Examples:
 *   -1.0;0.3; ;winrate;%.1f%% I'm losing
 *   -1.0;2.0;pasky;^when ;Today
 */
void chat_init(char *chat_file) {
}

void chat_done() {
}

/* Reply to a chat. When not playing, color is S_NONE and all remaining parameters are undefined.
 * If some matching entries have not yet been displayed we pick randomly among them. Otherwise
 * we pick randomly among all matching entries. */
char
*generic_chat(struct board *b, bool opponent, char *from, char *cmd, enum stone color, coord_t move,
	      int playouts, int machines, int threads, double winrate, double extra_komi) {
	return NULL;
}
