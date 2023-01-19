package bot;

import java.util.List;

public class BotConstants {
    public static final String NEWLINE = "\n";
    public static final String TAB = "\t";
    public static final String CLOSING_BRACKET = "}";

    public static final String INIT_BOT =
    """
    TOKEN$ = "MTAyMTE4OTQ3MDQ5OTkxODAxNA.GGCG5c.io2Fj2cGqcqb3QcHF45dHqnBmvPcg8p09BxkmM"
        
    const { Client, GatewayIntentBits } = require('discord.js');
    const client$ = new Client({
        intents: [
            GatewayIntentBits.Guilds,
            GatewayIntentBits.GuildMessages,
            GatewayIntentBits.MessageContent,
            GatewayIntentBits.GuildMembers,
        ],
    });
    client$.login(TOKEN$);
        
    // Only one conversation at a time
    let currAuthor$ = null;
    const isRunning$ = () => currAuthor$ !== null;
    const authorFilter$ = m => {
        return m.author == currAuthor$
    }
        
    client$.on('ready', () => {
        console.log(`Logged in as ${client$.user.tag}!`);
    });             
    """;

    // Trigger Phrases
    public static String createTriggerPhraseStart(int triggerPhraseNum) {
        return String.format(
        """
        async function triggerphrase%d$(message$) {
            currAuthor$ = message$.author;
            const channel$ = message$.channel;
                        
        """, triggerPhraseNum);
    }

    public static final String TRIGGER_PHRASE_END =
    """
        currAuthor$ = null;
    }
    """;

    public static String createMessageCreateBody(List<String> triggerPhrases) {
        StringBuilder output = new StringBuilder(
                """
                client$.on("messageCreate", (message$) => {
                    // Don't proceed if the bot input or bot already running convo
                    if (message$.author.bot || isRunning$()) return false;                                
                """);

        for (int i = 0; i < triggerPhrases.size(); i++) {
            String phrase = triggerPhrases.get(i);
            int phraseNum = i + 1;

            output.append(
                    String.format(
                    """
                        // Account for mentions of both the bot's ID and the bot's role
                        if (message$.content === "<@1021189470499918014> " + "%s" ||
                            message$.content === "<@&1023255278235435157> " + "%<s") {
                                triggerphrase%d$(message$);
                        }
                    """, phrase, phraseNum)
            );
        }

        return output.toString();
    }

    // Interactions
    public static String createSendMessage(String message) {
        return String.format("await channel$.send(\"\" + %s);", message);
    }

    public static String createResponseVarInit(String varName, boolean firstInit) {
        if (firstInit) {
            return String.format("let %s = \"\";", varName);
        } else {
            return String.format("%s = \"\";", varName);
        }
    }

    public static String createReceiveResponse(String responseVarName) {
        return String.format(
        """
        await channel$.awaitMessages({ authorFilter$, max: 1, time: 60_000, errors: ['time'] })
        .then(collected => {
            %s = collected.first().content;
        })
        .catch(collected => console.log(`Timed out after a minute`));
        """, responseVarName);
    }

    // Variables
    public static String createInitCounter(String varName, int value) {
        return String.format("let %s = %d;", varName, value);
    }

    public static String createIncCounter(String counterName) {
        return String.format("%s++;", counterName);
    }

    public static String createDecCounter(String counterName) {
        return String.format("%s--;", counterName);
    }

    // Function
    public static String createConversationStart(String conversationName) {
        return String.format(
        """
        async function %s(message$) {
            const channel$ = message$.channel;    
        """, conversationName);
    }

    public static String createFunctionalCall(String conversation) {
        return String.format("await %s(message$);", conversation);
    }

    public static String getUserName() {
        return """
        let userName$ = message$.member.toString()
        """;
    }

    public static String getUserId() {
        return """
        let userId$ = message$.member.id.toString()
        """;
    }

    public static String getUserRoles() {
        return """
        let userRole$ = message$.member.roles.cache.filter((roles) => roles.id !== message$.id)
        .map((role) => role.name.toString())[0]
        """;
    }
}
