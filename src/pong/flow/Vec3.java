package pong.flow;

public class Vec3 implements Comparable<Vec3>
{
	public float x, y, z, w;

	public Vec3()
	{
		x = y = z = w = 0.0f;
	}

	public Vec3(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = 0.0f;
	}
	

	public Vec3(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public float getMagnitude()
	{
		return (float) (Math.sqrt((double) ((x * x) + (y * y) + (z * z))));
	}

	public void Normalize()
	{
		x = x / getMagnitude();
		y = y / getMagnitude();
		z = z / getMagnitude();
	}
	
	public float Distance(Vec3 rhs)
	{
		return (float) (Math.sqrt((double) (((x - rhs.x)*(x - rhs.x)) + ((y - rhs.y)*(y - rhs.y)) + ((z - rhs.z)*(z - rhs.z)))));
	}

	public Vec3 Plus(Vec3 rhs)
	{
		return new Vec3(x + rhs.x, y + rhs.y, z + rhs.z);
	}
	
	public Vec3 Minus(Vec3 rhs)
	{
		return new Vec3(x - rhs.x, y - rhs.y, z - rhs.z);
	}

	public Vec3 Mult(Vec3 rhs)
	{
		return new Vec3(x * rhs.x, y * rhs.y, z * rhs.z);
	}
	
	public Vec3 Mult(float rhs)
	{
		return new Vec3(x * rhs, y * rhs, z * rhs);
	}
	
	public Vec3 Divide(Vec3 rhs)
	{
		return new Vec3(x / rhs.x, y / rhs.y, z / rhs.z);
	}

	public float Dot(Vec3 rhs)
	{
		return this.x * rhs.x + this.y * rhs.y + this.z * rhs.z;
	}
	
	public Vec3 Cross(Vec3 rhs)
	{
		return new Vec3((y*rhs.z - z *rhs.y), (z*rhs.x - x*rhs.z), (x*rhs.y - y*rhs.x));
	}
	
	public float[] getPrimitive()
	{
		float[] ret = {this.x, this.y, this.z, 1.0f};
		return ret;
	}

	@Override
	public String toString()
	{
		return "x: " + x + " y: " + y + " z: " + z;
	}


	public int compareTo(Vec3 another)
	{
		// TODO Auto-generated method stub
		return ((int) this.x - (int) another.x)
				+ ((int) this.y - (int) another.y)
				+ ((int) this.z - (int) another.z);
	}
}
