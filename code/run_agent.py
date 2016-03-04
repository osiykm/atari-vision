import os
import sys
from subprocess import Popen, PIPE

JAR_FILE = 'dist/ALEJavaAgent.jar'
ALE_FILE = './ale'


def run_agent(agent='human', gui=True, max_episodes=1, rom='space_invaders.bin'):
	# start environment
	environment = Popen([ALE_FILE, '-game_controller', 'fifo', 'roms/'+rom], stdin=PIPE, stdout=PIPE)

	# start agent
	gui_option = '' if gui else '-nogui'
	agent = Popen(['java', '-Xmx1024M', '-jar', JAR_FILE, '-agent', agent], stdin=PIPE, stdout=PIPE)


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
		[t, r] = data[1].split(',')
		term = int(t)
		reward = int(r);
		cum_reward += reward
		if term:
			print 'Episode ' + str(episode) + ': ' + str(cum_reward)
			cum_rewards.append(cum_reward)
			episode += 1

			# break at the end of experiment
			if episode > max_episodes:
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
	run_agent(agent='random')

