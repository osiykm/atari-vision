import os
import sys
from subprocess import Popen, PIPE

JAR_FILE = 'dist/ALEJavaAgent.jar'
ALE_FILE = './ale'
ROM_FOLDER = 'roms/'


def run_agent(agent='human', gui=True, max_episodes=1, rom='space_invaders.bin'):
	# start environment
	environment = Popen([ALE_FILE, '-game_controller', 'fifo', ROM_FOLDER+rom], stdin=PIPE, stdout=PIPE)

	# start agent
	agent_args = ['java', '-Xmx1024M', '-jar', JAR_FILE, '-agent', agent]
	if not gui:
		agent_args.append('-nogui')

	agent = Popen(agent_args, stdin=PIPE, stdout=PIPE)


	cum_reward = 0
	cum_rewards = []
	episode = 1

	# handshake
	# read from environment, send to agent
	l = environment.stdout.readline()
	agent.stdin.write(l)
	# read from agent, send to environment
	a = agent.stdout.readline()
	environment.stdin.write(a)
	
	while(1):
		# check for premature environment termination
		if environment.poll() != None:
			agent.kill()
			break

		# read from environment, send to agent
		l = environment.stdout.readline()
		agent.stdin.write(l)

		# extract and record reward and terminal status
		data = l.split(':')
		[t, r] = data[-2].split(',')
		term = int(t)
		reward = int(r);
		cum_reward += reward
		if term:
			print 'Episode ' + str(episode) + ': ' + str(cum_reward)
			cum_rewards.append(cum_reward)
			episode += 1

			# break at the end of experiment
			if episode > max_episodes:
				print 'FINISHED'
				break
			cum_reward = 0

		# check for premature agent termination
		if agent.poll() != None:
			environment.kill()
			break

		# read from agent
		a = agent.stdout.readline()

		# check if agent is finished
		if not a:
			break;

		# send to environment
		environment.stdin.write(a)

	return cum_rewards


if __name__ == '__main__':
	import argparse

	parser = argparse.ArgumentParser()
	parser.add_argument("--agent", default='human',
						help="the name of the agent to use (all lowercase)")
	parser.add_argument("--episodes", default=1, type=int,
						help="the number of episodes to test")
	parser.add_argument("--rom", default='space_invaders.bin',
						help="the name of the rom file to run")
	parser.add_argument("-n", "--nogui", action="store_true",
						help="flag to turn off gui for faster running")


	args = parser.parse_args()
	agent = args.agent
	episodes = args.episodes
	rom = args.rom
	gui = not args.nogui

	run_agent(agent=agent, gui=gui, max_episodes=episodes, rom=rom)

