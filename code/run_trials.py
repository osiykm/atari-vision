import run_agent
import numpy as np

all_agents = ["random", "naive"]
numtrials = 10
rom = 'space_invaders.bin'

def run_trials(agents):
    agent_rewards = {}

    for agent in agents:
        print "Collecting data for agent: " + agent

        agent_rewards[agent] = run_agent.run_agent(agent=agent, gui=False, max_episodes=numtrials, rom=rom)

    print "All results:"
    for agent in agents:
        print agent + ":"
        rewards = np.array(agent_rewards[agent])
        print "average reward: " + str(np.average(rewards))
        print "average std dev: " + str(np.std(rewards))

        print

if __name__ == '__main__':

    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument("--agent", default='all',
                        help="the name of the agent to use (all lowercase) or 'all' to go through all agents")

    args = parser.parse_args()
    if (args.agent == 'all'):
        agents = all_agents
    else:
        agents = [args.agent]

    run_trials(agents)
