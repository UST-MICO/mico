package io.github.ust.mico.core.mapping;

import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * Represents the information necessary for deploying
 * a {@link MicoApplication}.
 */
@Data
@AllArgsConstructor
@Builder
@NodeEntity
public class MicoServiceDeploymentInfo {
	
	@Id
	@GeneratedValue
	private long id;
	
	// The id of the parent service.
	@ApiModelProperty(required = true)
	private long serviceId;

	
	// ----------------------
	// -> Required fields ---
	// ----------------------
	
	// Number of desired instances. Defaults to 1.
	@ApiModelProperty(required = true)
	@Default
	private int serviceReplicas = 1;
	
	// The list of containers to run within this service.
	@ApiModelProperty(required = true)
	private List<Container> containers;
	
	
	
	// ----------------------
	// -> Optional fields ---
	// ----------------------
	
	// Minimum number of seconds for which this service should be ready
	// without any of its containers crashing, for it to be considered available.
	// Defaults to 0 (considered available as soon as it is ready).
	@Default
	private int minReadySecondsBeforeMarkedAvailable = 0;
	
	// Those labels are key-value pairs that are attached to the deployment
	// of this service. Intended to be used to specify identifying attributes
	// that are meaningful and relevant to users, but do not directly imply
	// semantics to the core system. Labels can be used to organize and to select
	// subsets of objects. Labels can be attached to objects at creation time and
	// subsequently added and modified at any time.
	// Each key must be unique for a given object.
	// Defaults to [ {"app" -> "Service#shortName"} ].
	private Map<String, String> labels;
	
	// Indicates whether and when to pull the image.
	// Defaults to ImagePullPolicy#DEFAULT.
	@Default
	private ImagePullPolicy imagePullPolicy = ImagePullPolicy.DEFAULT;
	
	// Restart policy for all containers.
	// Defaults to RestartPolicy#ALWAYS.
	@Default
	private RestartPolicy restartPolicy = RestartPolicy.DEFAULT;
	
	
	/**
	 * The deployment strategy to use to replace
	 * existing services with new ones.
	 */
	@Data
	@AllArgsConstructor
	@Builder
	public static class Strategy {
		
		// ----------------------
		// -> Required fields ---
		// ----------------------
		
		// The type of this deployment strategy, can
		// RECREATE or ROLLING_UPDATE.
		// Defaults to Type#DEFAULT.
		@Default
		private Type type = Type.DEFAULT;
		
		
		// ----------------------
		// -> Optional fields ---
		// ----------------------
		
		// The maximum number of instances that can be scheduled above the desired number of
		// instances during the update. Value can be an absolute number or a percentage of
		// desired instances. This can not be 0 if maxUnavailable is 0. Absolute number is
		// calculated from percentage by rounding up.
		// If both fields are specified, the percentage will be used.
		// Defaults to 25%.
		@Default
		private double maxInstancesOnTopPercent = 0.25;
		private double maxInstancesOnTopAbsolute;
		
		// The maximum number of instances that can be unavailable during the update.
		// Value can be an absolute number or a percentage of desired pods. Absolute number is
		// calculated from percentage by rounding down. This can not be 0 if MaxSurge is 0.
		// If both fields are specified, the percentage will be used.
		// Defaults to 25%.
		@Default
		private double maxInstancesBelowPercent = 0.25;
		private double maxInstancesBelow;
		
		
		/**
		 * Enumeration for the supported types of deployment strategies.
		 */
		public enum Type {
			
			/** Delete all running instances and then create new ones. */
			RECREATE,
			/** Update one after the other. */
			ROLLING_UPDATE;
			
			/** Default deployment strategy type is {@link Type#ROLLING_UPDATE}. */
			public static Type DEFAULT = Type.ROLLING_UPDATE;
			
		}
		
	}
	
	/**
	 * Enumeration for the different policies specifying
	 * when to pull an image.
	 */
	public enum ImagePullPolicy {
		
		ALWAYS,
		NEVER,
		IF_NOT_PRESENT;

		/** Default image pull policy is {@link ImagePullPolicy#ALWAYS}. */
		public static ImagePullPolicy DEFAULT = ImagePullPolicy.ALWAYS;
		
	}
	
	/**
	 * Represents a container running in a Kubernetes Pod.
	 */
	@Data
	@AllArgsConstructor
	@Builder
	public static class Container {
		
		// The name of the container (in the Kubernetes Pod).
		// Defaults to Service#shortName.
		@ApiModelProperty(required = true)
		private String name;
		
		// The name of the docker image.
		// Defaults to Service#shortName.
		@ApiModelProperty(required = true)
		private String image;
		
		// The list of ports for this service.
		@ApiModelProperty(required = true)
		private List<MicoPort> ports;
		
		// Limit describing the minimum amount of compute
		// resources allowed. If omitted it defaults to the
		// upper limit if that is explicitly specified.
		private ResourceConstraint resourceLowerLimit;
		
		// Limit describing the maximum amount of compute
		// resources allowed.
		private ResourceConstraint resourceUpperLimit;
		
		// Indicates whether this container should have
		// a read-only root file system. Defaults to false.
		@Default
		private boolean readOnlyRootFileSystem = false;
		
		// Indicates whether the service must run as a non-root user.
		// If somehow not run as non-root user (not UID 0) it will
		// fail to start. Default to false.
		@Default
		private boolean runAsNonRoot = false;
		
	}
	
	/**
	 * Represents a resource constraint specifying the CPU units
	 * and memory. Can be used as a upper (limiting) and
	 * lower (requesting) constraint. 
	 */
	@Data
	@AllArgsConstructor
	@Builder
	public static class ResourceConstraint {
		
		// Measured in CPU units. One Kubernetes CPU (unit) is equivaletnt to:
		//  - 1 AWS vCPU
		//  - 1 GCP Core
		//  - 1 Azure vCore
		//  - 1 IBM vCPU
		//  - 1 Hyperthread on a bare-metal Intel processor with Hyperthreading
		// Can also be specified as a fraction up to precision 0.001.
		private double cpuUnits;
		
		// Memory in bytes.
		private long memoryInBytes;
		
	}
	
	/**
	 * Enumeration for all supported restart policies.
	 */
	public enum RestartPolicy {
		
		ALWAYS,
		ON_FAILURE,
		NEVER;

		/** Default restart policy is {@link RestartPolicy#ALWAYS}. */
		public static RestartPolicy DEFAULT = RestartPolicy.ALWAYS;
		
	}
	
}

