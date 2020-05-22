package app.client.ui.animations;

import java.util.concurrent.atomic.AtomicBoolean;

import app.client.ui.Console;

public abstract class AnimatedText extends Thread
{
	protected String text;
	protected AtomicBoolean running;

	public AnimatedText(String text)
	{
		this.text = text;
		this.running = new AtomicBoolean();
	}

	@Override
	public void run()
	{
		running.set(true);
		while (isRunning())
			animate();
	}

	public boolean isRunning()
	{
		return running.get();
	}

	protected abstract void animate();

	@Override
	public void start()
	{
		super.start();
		while (!isAlive() || !isRunning())
			;
	}

	public void stopShowing()
	{
		if (!isAlive())
			return;
		if (!running.getAndSet(false))
			return;
		try {
			this.join();
		} catch (InterruptedException e) {
		}
		Console.newLine();
	}
}
