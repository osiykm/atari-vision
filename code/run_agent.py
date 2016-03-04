import os
import sys
from subprocess import Popen, PIPE

JAR_FILE = 'dist/ALEJavaAgent.jar'


def run_agent(rom='space_invaders.bin'):
	environment = Popen(['./ale', '-game_controller', 'fifo', 'roms/'+rom], stdin=PIPE, stdout=PIPE)
	agent = Popen(['java', '-Xmx1024M', '-jar', JAR_FILE], stdin=PIPE, stdout=PIPE)

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
		[t, r] = l.split(':')[1].split(',')
		term = int(t)
		reward = int(r);
		cum_reward += reward
		if term:
			print 'Episode ' + str(episode) + ': ' + str(cum_reward)
			cum_rewards.append(cum_reward)
			episode += 1
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
	run_agent()

