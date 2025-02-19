package info.itsthesky.disky.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import info.itsthesky.disky.DiSky;
import info.itsthesky.disky.api.skript.SpecificBotEffect;
import info.itsthesky.disky.core.Bot;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Move Role Above/Under Role")
@Description({"Move a specific role above or under another role within the same guild.",
"The indexes will be updated automatically.".
"- You cannot move roles above the bot's highest role!"})
@Examples("move role {_role} above role with id \"000\"")
public class MoveRole extends SpecificBotEffect {

	static {
		Skript.registerEffect(
				MoveRole.class,
				"move [the] [discord] role %role% above [the] [discord] %role%",
				"move [the] [discord] role %role% under [the] [discord] %role%"
		);
	}

	private Expression<Role> exprTarget;
	private Expression<Role> exprRole;
	private boolean isAbove;

	@Override
	public boolean initEffect(Expression[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
		exprTarget = (Expression<Role>) expressions[0];
		exprRole = (Expression<Role>) expressions[1];
		isAbove = i == 0;
		return true;
	}

	@Override
	public void runEffect(@NotNull Event e, @NotNull Bot bot) {
		final Role target = parseSingle(exprTarget, e);
		final Role role = parseSingle(exprRole, e);
		if (target == null || role == null) return;

		if (target.getGuild().getIdLong() != role.getGuild().getIdLong()) {
			Skript.error("The specified roles are not in the same guild!");
			return;
		}

		RoleOrderAction action = target.getGuild().modifyRolePositions();
		if (isAbove)
			action = action.moveAbove(role);
		else
			action = action.moveBelow(role);

		action.queue(this::restart, ex -> {
			DiSky.getErrorHandler().exception(e, ex);
			restart();
		});
	}

	@Override
	public @NotNull String toString(@Nullable Event e, boolean debug) {
		return "move role " + exprTarget.toString(e, debug) + " " + (isAbove ? "above" : "under") + " role " + exprRole.toString(e, debug);
	}
}
